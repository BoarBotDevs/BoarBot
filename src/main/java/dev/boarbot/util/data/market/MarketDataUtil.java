package dev.boarbot.util.data.market;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.util.boar.BoarTag;
import dev.boarbot.util.logging.Log;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class MarketDataUtil implements Configured {
    private static final Semaphore semaphore = new Semaphore(1);

    public static Map<String, List<MarketData>> getAllItemData(Connection connection) throws SQLException {
        Map<String, List<MarketData>> allItemData = new HashMap<>();

        String query = """
            SELECT item_id
            FROM market;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String itemID = resultSet.getString("item_id");
                    allItemData.put(itemID, getItemData(itemID, false, connection));
                }
            }
        }

        return allItemData;
    }

    public static List<MarketData> getItemData(
        String itemID, boolean updateCache, Connection connection
    ) throws SQLException {
        List<MarketData> itemData = new ArrayList<>();

        String query = """
            SELECT user_id, amount, price, edition
            FROM market
            WHERE item_id = ?
            ORDER BY price, listed_timestamp;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    itemData.add(new MarketData(
                        resultSet.getString("user_id"),
                        resultSet.getLong("amount"),
                        resultSet.getLong("price"),
                        resultSet.getLong("edition")
                    ));
                }
            }
        }

        if (updateCache) {
            MarketInteractive.cachedMarketData.put(itemID, itemData);
        }

        return itemData;
    }

    private static void updateItemData(String itemID, Connection connection) throws SQLException {
        getItemData(itemID, true, connection);
    }

    public static void sellOfferItem(
        String itemID, int amount, long price, BoarUser boarUser, Connection connection
    ) throws SQLException {
        semaphore.acquireUninterruptibly();

        String powOfferQuery = """
            SELECT 1
            FROM market
            WHERE item_id = ? AND price = ? AND user_id = ?;
        """;

        String powOfferUpdate = """
            UPDATE market
            SET amount += ?
            WHERE item_id = ? AND price = ? AND user_id = ?;
        """;

        String powOfferInsert = """
            INSERT INTO market (item_id, price, amount, user_id)
            VALUES (?, ?, ?, ?);
        """;

        String boarQuery = """
            SELECT edition
            FROM collected_boars
            WHERE boar_id = ? AND user_id = ? AND deleted = false AND `exists` = true
            ORDER BY edition DESC LIMIT ?;
        """;

        String boarAddQuery = """
            INSERT INTO market (item_id, edition, price, user_id)
            VALUES (?, ?, ?, ?);
        """;

        String removeBoarQuery = """
            UPDATE collected_boars
            SET deleted = true
            WHERE boar_id = ? AND user_id = ? AND edition = ?;
        """;

        try {
            if (POWS.containsKey(itemID)) {
                try (
                    PreparedStatement statement1 = connection.prepareStatement(powOfferQuery);
                    PreparedStatement statement2 = connection.prepareStatement(powOfferUpdate);
                    PreparedStatement statement3 = connection.prepareStatement(powOfferInsert)
                ) {
                    boolean addToExisting;

                    statement1.setString(1, itemID);
                    statement1.setLong(2, price);
                    statement1.setString(3, boarUser.getUserID());

                    try (ResultSet resultSet = statement1.executeQuery()) {
                        addToExisting = resultSet.next();
                    }

                    if (addToExisting) {
                        statement2.setLong(1, amount);
                        statement2.setString(2, itemID);
                        statement2.setLong(3, price);
                        statement2.setString(4, boarUser.getUserID());
                    } else {
                        statement3.setString(1, itemID);
                        statement3.setLong(2, price);
                        statement3.setLong(3, amount);
                        statement3.setString(4, boarUser.getUserID());
                    }
                }

                boarUser.powQuery().usePowerup(connection, itemID, amount, false);
            } else {
                List<Long> editions = new ArrayList<>();

                try (
                    PreparedStatement statement1 = connection.prepareStatement(boarQuery);
                    PreparedStatement statement2 = connection.prepareStatement(boarAddQuery);
                    PreparedStatement statement3 = connection.prepareStatement(removeBoarQuery)
                ) {
                    statement1.setString(1, itemID);
                    statement1.setString(2, boarUser.getUserID());
                    statement1.setInt(3, amount);

                    try (ResultSet resultSet = statement1.executeQuery()) {
                        while (resultSet.next()) {
                            editions.add(resultSet.getLong("edition"));
                        }
                    }

                    for (Long edition : editions) {
                        statement2.setString(1, itemID);
                        statement2.setLong(2, edition);
                        statement2.setLong(3, price);
                        statement3.setString(4, boarUser.getUserID());
                        statement2.addBatch();
                    }

                    statement2.executeBatch();

                    for (Long edition : editions) {
                        statement3.setString(1, itemID);
                        statement3.setString(2, boarUser.getUserID());
                        statement3.setLong(3, edition);
                        statement3.addBatch();
                    }

                    statement3.executeBatch();
                }
            }

            updateItemData(itemID, connection);
        } finally {
            semaphore.release();
        }
    }

    public static void buyItem(
        String itemID, int amount, long price, BoarUser boarUser, Connection connection
    ) throws SQLException, IllegalStateException {
        semaphore.acquireUninterruptibly();

        String boarGiveQuery = """
            UPDATE collected_boars
            SET user_id = ?, deleted = false, tag = %s
            WHERE boar_id = ? AND edition = ?;
        """.formatted(BoarTag.MARKET.toString());

        String updateOffers = """
            UPDATE market
            SET amount = amount - ?
            WHERE item_id = ? AND price = ? AND edition = ? AND user_id = ?;
        """;

        String deleteOffers = """
            DELETE FROM market
            WHERE amount <= 0;
        """;

        try {
            List<MarketData> itemData = getItemData(itemID, false, connection);
            List<MarketData> transactionItemData = new ArrayList<>();

            long actualAmount = 0;
            long actualPrice = 0;

            for (MarketData marketData : itemData) {
                if (actualAmount == amount) {
                    break;
                }

                long curAmount = Math.min(marketData.amount(), amount - actualAmount);

                actualAmount += curAmount;
                actualPrice += curAmount * marketData.price();

                if (marketData.amount() < curAmount) {
                    transactionItemData.add(new MarketData(
                        marketData.userID(), curAmount, marketData.price(), marketData.edition()
                    ));
                } else {
                    transactionItemData.add(marketData);
                }
            }

            if (actualAmount < amount) {
                throw new IllegalStateException("amount");
            }

            if (actualPrice > price) {
                throw new IllegalStateException("price");
            }

            if (POWS.containsKey(itemID)) {
                boarUser.powQuery().addPowerup(connection, itemID, amount);
            } else {
                try (PreparedStatement statement = connection.prepareStatement(boarGiveQuery)) {
                    for (MarketData marketData : transactionItemData) {
                        statement.setString(1, boarUser.getUserID());
                        statement.setString(2, itemID);
                        statement.setLong(3, marketData.edition());
                        statement.addBatch();
                    }

                    statement.executeBatch();
                }
            }

            boarUser.baseQuery().useBucks(connection, actualPrice);

            try (
                PreparedStatement statement1 = connection.prepareStatement(updateOffers);
                PreparedStatement statement2 = connection.prepareStatement(deleteOffers)
            ) {
                for (MarketData marketData : transactionItemData) {
                    if (!boarUser.getUserID().equals(marketData.userID())) {
                        BoarUser sellUser = BoarUserFactory.getBoarUser(marketData.userID());
                        sellUser.passSynchronizedAction(() -> {
                            try {
                                sellUser.baseQuery().giveBucks(connection, marketData.price() * marketData.amount());
                            } catch (SQLException exception) {
                                Log.error(MarketData.class, "Failed to give bucks to " + sellUser.getUserID(), exception);
                            }
                        });
                    } else {
                        boarUser.baseQuery().giveBucks(connection, marketData.price() * marketData.amount());
                    }


                    statement1.setLong(1, marketData.amount());
                    statement1.setString(2, itemID);
                    statement1.setLong(3, marketData.price());
                    statement1.setLong(4, marketData.edition());
                    statement1.setString(5, marketData.userID());
                    statement1.addBatch();
                }

                statement1.executeBatch();
                statement2.executeUpdate();
            }

            updateItemData(itemID, connection);
        } finally {
            semaphore.release();
        }
    }
}
