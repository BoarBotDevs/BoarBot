package dev.boarbot.util.data.market;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.util.boar.BoarTag;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.time.TimeUtil;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class MarketDataUtil implements Configured {
    private static final Semaphore semaphore = new Semaphore(1);

    public static Map<String, List<Long>> getAllItemPrices(Connection connection) throws SQLException {
        Map<String, List<Long>> prices = new LinkedHashMap<>();

        String query = """
            SELECT item_id, price
            FROM market
            ORDER BY price, listed_timestamp;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prices.putIfAbsent(
                        resultSet.getString("item_id"),
                        new ArrayList<>()
                    );

                    prices.get(resultSet.getString("item_id")).add(resultSet.getLong("price"));
                }
            }
        }

        return prices;
    }

    public static List<Long> getItemPrices(
        String itemID, boolean updateCache, Connection connection
    ) throws SQLException {
        List<Long> prices = new ArrayList<>();

        String query = """
            SELECT price,
            FROM market
            WHERE item_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prices.add(resultSet.getLong("price"));
                }
            }
        }

        if (updateCache) {
            MarketInteractive.cachedMarketData.put(itemID, prices);
        }

        return prices;
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
            SET deleted = true, tag = %s
            WHERE boar_id = ? AND user_id = ? AND edition = ?;
        """.formatted(BoarTag.MARKET.toString());

        try {
            if (POWS.containsKey(itemID)) {
                try (
                    PreparedStatement statement1 = connection.prepareStatement(powOfferQuery);
                    PreparedStatement statement2 = connection.prepareStatement(powOfferUpdate);
                    PreparedStatement statement3 = connection.prepareStatement(powOfferInsert);
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
                    PreparedStatement statement3 = connection.prepareStatement(removeBoarQuery);
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

            getItemPrices(itemID, true, connection);
        } finally {
            semaphore.release();
        }
    }

    public static void buyItem(
        String itemID, int amount, BoarUser boarUser, Connection connection
    ) throws SQLException {
        semaphore.acquireUninterruptibly();

    }

    private static MarketTransactionFail buyItem(
        String itemID, int amount, long cost, BoarUser boarUser, Connection connection
    ) throws SQLException {
        MarketData marketData = getMarketDataItem(itemID, false, connection);
        MarketTransactionData buyData = calculateBuyCost(itemID, marketData, amount);

        if (buyData.cost() > cost) {
            return MarketTransactionFail.COST;
        }

        if (buyData.stock() < 0) {
            return MarketTransactionFail.STOCK;
        }

        marketData = new MarketData(
            buyData.stock(),
            buyData.sellPrice(),
            buyData.buyPrice(),
            marketData.lastPurchase(),
            marketData.lastSell()
        );

        String updateQuery = """
            UPDATE market_values
            SET stock = ?, sell_price = ?, buy_price = ?, last_purchase = current_timestamp(3)
            WHERE item_id = ?;
        """;

        String editionQuery = """
            SELECT edition
            FROM market_editions
            WHERE item_id = ?
            LIMIT ?;
        """;

        String editionRemoveQuery = """
            DELETE FROM market_editions
            WHERE item_id = ? AND edition = ?;
        """;

        String editionGiveQuery = """
            UPDATE collected_boars
            SET user_id = ?, deleted = false
            WHERE boar_id = ? AND edition = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, marketData.stock());
            statement.setLong(2, marketData.sellPrice());
            statement.setLong(3, marketData.buyPrice());
            statement.setString(4, itemID);
            statement.executeUpdate();
        }

        if (POWS.containsKey(itemID)) {
            boarUser.powQuery().addPowerup(connection, itemID, amount);
        } else {
            List<Integer> editions = new ArrayList<>();

            try (
                PreparedStatement statement1 = connection.prepareStatement(editionQuery);
                PreparedStatement statement2 = connection.prepareStatement(editionRemoveQuery);
                PreparedStatement statement3 = connection.prepareStatement(editionGiveQuery)
            ) {
                statement1.setString(1, itemID);
                statement1.setInt(2, amount);

                try (ResultSet resultSet = statement1.executeQuery()) {
                    while (resultSet.next()) {
                        editions.add(resultSet.getInt("edition"));
                    }
                }

                for (Integer edition : editions) {
                    statement2.setString(1, itemID);
                    statement2.setInt(2, edition);
                    statement2.addBatch();
                }

                statement2.executeBatch();

                for (Integer edition : editions) {
                    statement3.setString(1, boarUser.getUserID());
                    statement3.setString(2, itemID);
                    statement3.setInt(3, edition);
                    statement3.addBatch();
                }

                statement3.executeBatch();
            }

            List<String> itemIDs = new ArrayList<>();

            for (int i=0; i<amount-editions.size(); i++) {
                itemIDs.add(itemID);
            }

            boarUser.boarQuery().addBoars(
                itemIDs,
                connection,
                BoarTag.MARKET.toString(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashSet<>()
            );
        }

        boarUser.baseQuery().useBucks(connection, buyData.cost());

        MarketInteractive.cachedMarketData.put(itemID, marketData);
        return null;
    }
}
