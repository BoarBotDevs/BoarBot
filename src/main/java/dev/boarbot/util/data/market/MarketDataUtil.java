package dev.boarbot.util.data.market;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.time.TimeUtil;

import java.sql.*;
import java.util.*;

public class MarketDataUtil implements Configured {
    public static Map<String, MarketData> getMarketData(Connection connection) throws SQLException {
        Map<String, MarketData> marketData = new LinkedHashMap<>();

        String query = """
            SELECT item_id, stock, sell_price, buy_price
            FROM market_values;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    marketData.put(
                        resultSet.getString("item_id"),
                        new MarketData(
                            resultSet.getInt("stock"),
                            resultSet.getLong("sell_price"),
                            resultSet.getLong("buy_price")
                        )
                    );
                }
            }
        }

        return marketData;
    }

    public static MarketData getMarketDataItem(
        String itemID, boolean updateCache, Connection connection
    ) throws SQLException {
        MarketData marketData = null;

        String query = """
            SELECT stock, sell_price, buy_price
            FROM market_values
            WHERE item_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    marketData = new MarketData(
                        resultSet.getInt("stock"),
                        resultSet.getLong("sell_price"),
                        resultSet.getLong("buy_price")
                    );
                }
            }
        }

        if (updateCache) {
            MarketInteractive.cachedMarketData.put(itemID, marketData);
        }

        return marketData;
    }

    public synchronized static void updateMarket(
        MarketUpdateType updateType, Connection connection
    ) throws SQLException {
        updateMarket(updateType, null, 0, 0, null, connection);
    }

    public synchronized static void updateMarket(
        MarketUpdateType updateType, String itemID, Connection connection
    ) throws SQLException {
        updateMarket(updateType, itemID, 0, 0, null, connection);
    }

    public synchronized static MarketTransactionFail updateMarket(
        MarketUpdateType updateType, String itemID, int amount, long cost, BoarUser boarUser, Connection connection
    ) throws SQLException {
        return switch (updateType) {
            case AUTO_ADJUST -> {
                autoAdjustMarketData(connection);
                yield null;
            }

            case ADD_ITEM -> {
                addMarketDataItem(itemID, connection);
                yield null;
            }

            case BUY_ITEM -> buyItem(itemID, amount, cost, boarUser, connection);

            case SELL_ITEM -> sellItem(itemID, amount, cost, boarUser, connection);
        };
    }

    private static void addMarketDataItem(String itemID, Connection connection) throws SQLException {
        String updateQuery = """
            INSERT INTO market_values (item_id, stock, buy_price)
            SELECT ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM market_values
                WHERE item_id = ?
            );
        """;

        boolean isPowerup = POWS.containsKey(itemID);
        String rarityKey = isPowerup ? null : BoarUtil.findRarityKey(itemID);
        Integer targetStock = isPowerup
            ? POWS.get(itemID).getTargetStock()
            : RARITIES.get(rarityKey).getTargetStock();

        if (targetStock == null) {
            return;
        }

        long buyPrice = isPowerup || RARITIES.get(rarityKey).getBaseBucks() == 0
            ? NUMS.getBuyPriceStart()
            : RARITIES.get(rarityKey).getBaseBucks() * 100;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, itemID);
            statement.setInt(2, targetStock);
            statement.setLong(3, buyPrice);
            statement.setString(4, itemID);
            statement.executeUpdate();
        }

        MarketInteractive.cachedMarketData.put(itemID, new MarketData(targetStock, buyPrice, 1));
    }

    private static void autoAdjustMarketData(Connection connection) throws SQLException {
        String query = """
            SELECT item_id, stock, sell_price, buy_price, last_purchase, last_sell
            FROM market_values;
        """;

        String updateQuery = """
            UPDATE market_values
            SET buy_price = ?, sell_price = ?, stock = stock + ?
            WHERE item_id = ?;
        """;

        Map<String, MarketData> updateBuyItems = new HashMap<>();
        Map<String, MarketData> updateSellItems = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String itemID = resultSet.getString("item_id");
                    Timestamp lastPurchase = resultSet.getTimestamp("last_purchase");
                    Timestamp lastSell = resultSet.getTimestamp("last_sell");
                    int stock = resultSet.getInt("stock");
                    long buyPrice = resultSet.getLong("buy_price");
                    long sellPrice = resultSet.getLong("sell_price");

                    if (lastPurchase == null) {
                        updateBuyItems.put(itemID, new MarketData(stock, sellPrice, buyPrice));
                    }

                    if (lastSell == null) {
                        updateSellItems.put(itemID, new MarketData(stock, sellPrice, buyPrice));
                    }

                    if (lastPurchase == null || lastSell == null) {
                        continue;
                    }

                    long waitTime = POWS.containsKey(itemID)
                        ? POWS.get(itemID).getPriceAdjustWaitHours() * 1000 * 60 * 60
                        : RARITIES.get(BoarUtil.findRarityKey(itemID)).getPriceAdjustWaitHours() * 1000 * 60 * 60;
                    boolean shouldUpdateBuy = lastPurchase.getTime() + waitTime <= TimeUtil.getCurMilli();
                    boolean shouldUpdateSell = lastSell.getTime() + waitTime <= TimeUtil.getCurMilli();

                    if (shouldUpdateBuy) {
                        updateBuyItems.put(itemID, new MarketData(stock, sellPrice, buyPrice));
                    }

                    if (shouldUpdateSell) {
                        updateSellItems.put(itemID, new MarketData(stock, sellPrice, buyPrice));
                    }
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            Set<String> updateItems = new HashSet<>();
            updateItems.addAll(updateBuyItems.keySet());
            updateItems.addAll(updateSellItems.keySet());

            for (String itemID : updateItems) {
                boolean buyFix = updateBuyItems.containsKey(itemID);
                boolean sellFix = updateSellItems.containsKey(itemID);

                long newBuyPrice = 0;
                long newSellPrice = 0;
                int stock = 0;

                if (sellFix) {
                    MarketData marketData = updateSellItems.get(itemID);

                    long potentialSell = (long) Math.max(
                        marketData.sellPrice() * (1 + NUMS.getPriceAdjustPercent()), NUMS.getSellPriceMinimum()
                    );

                    long maxSell = (long) Math.max(
                        newBuyPrice * (1-NUMS.getPriceDiffPercent()), NUMS.getSellPriceMinimum()
                    );

                    newBuyPrice = marketData.buyPrice();
                    newSellPrice = Math.min(potentialSell, maxSell);
                    stock = marketData.stock();
                }

                if (buyFix) {
                    MarketData marketData = updateBuyItems.get(itemID);

                    long curSellPrice = newSellPrice == 0
                        ? marketData.sellPrice()
                        : newSellPrice;

                    newBuyPrice = (long) Math.max(
                        marketData.buyPrice() / (1 + NUMS.getPriceAdjustPercent()), NUMS.getBuyPriceMinimum()
                    );

                    newSellPrice = curSellPrice > newBuyPrice * (1-NUMS.getPriceDiffPercent())
                        ? (long) Math.max(newBuyPrice * (1-NUMS.getPriceDiffPercent()), NUMS.getSellPriceMinimum())
                        : curSellPrice;

                    stock = marketData.stock();
                }

                statement.setLong(1, newBuyPrice);
                statement.setLong(2, newSellPrice);
                statement.setInt(3, stock == 0 && buyFix ? 1 : 0);
                statement.setString(4, itemID);
                statement.addBatch();
            }

            statement.executeBatch();
        }
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

        marketData = new MarketData(buyData.stock(), buyData.sellPrice(), buyData.buyPrice());

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
                BoarObtainType.OTHER.toString(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashSet<>()
            );
        }

        boarUser.baseQuery().useBucks(connection, buyData.cost());

        MarketInteractive.cachedMarketData.put(itemID, marketData);
        return null;
    }

    private static MarketTransactionFail sellItem(
        String itemID, int amount, long cost, BoarUser boarUser, Connection connection
    ) throws SQLException {
        MarketData marketData = getMarketDataItem(itemID, false, connection);
        MarketTransactionData sellData = calculateSellCost(itemID, marketData, amount);

        if (sellData.cost() < cost) {
            return MarketTransactionFail.COST;
        }

        marketData = new MarketData(sellData.stock(), sellData.sellPrice(), sellData.buyPrice());

        String updateQuery = """
            UPDATE market_values
            SET stock = ?, sell_price = ?, buy_price = ?, last_sell = current_timestamp(3)
            WHERE item_id = ?;
        """;

        String editionQuery = """
            SELECT edition
            FROM collected_boars
            WHERE boar_id = ? AND user_id = ? AND deleted = false AND `exists` = true
            ORDER BY edition DESC LIMIT ?;
        """;

        String editionAddQuery = """
            INSERT INTO market_editions (item_id, edition)
            VALUES (?, ?);
        """;

        String removeBoarQuery = """
            UPDATE collected_boars
            SET deleted = true
            WHERE boar_id = ? AND user_id = ? AND edition = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, marketData.stock());
            statement.setLong(2, marketData.sellPrice());
            statement.setLong(3, marketData.buyPrice());
            statement.setString(4, itemID);
            statement.executeUpdate();
        }

        if (POWS.containsKey(itemID)) {
            boarUser.powQuery().usePowerup(connection, itemID, amount, false);
        } else {
            List<Integer> editions = new ArrayList<>();

            try (
                PreparedStatement statement1 = connection.prepareStatement(editionQuery);
                PreparedStatement statement2 = connection.prepareStatement(editionAddQuery);
                PreparedStatement statement3 = connection.prepareStatement(removeBoarQuery)
            ) {
                statement1.setString(1, itemID);
                statement1.setString(2, boarUser.getUserID());
                statement1.setInt(3, amount);

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
                    statement3.setString(1, itemID);
                    statement3.setString(2, boarUser.getUserID());
                    statement3.setInt(3, edition);
                    statement3.addBatch();
                }

                statement3.executeBatch();
            }
        }

        boarUser.baseQuery().giveBucks(connection, sellData.cost());

        MarketInteractive.cachedMarketData.put(itemID, marketData);
        return null;
    }

    public static MarketTransactionData calculateBuyCost(String itemID, MarketData marketData, int amount) {
        boolean isPowerup = POWS.containsKey(itemID);
        String rarityKey = isPowerup ? null : BoarUtil.findRarityKey(itemID);
        int targetStock = isPowerup
            ? POWS.get(itemID).getTargetStock()
            : RARITIES.get(rarityKey).getTargetStock();

        int fixedAmount = Math.min(amount, marketData.stock());
        int addedAmount = 0;
        int newStock = marketData.stock();
        long cost = 0;
        long prevSellPrice = marketData.sellPrice();
        long newSellPrice = prevSellPrice;
        long prevBuyPrice = marketData.buyPrice();
        long newBuyPrice = prevBuyPrice;

        if (newStock > targetStock) {
            addedAmount = Math.min(fixedAmount, newStock - targetStock);
            cost = prevBuyPrice * addedAmount;
            newStock -= addedAmount;
        }

        for (int i=0; i<fixedAmount-addedAmount; i++) {
            newStock--;
            cost += newBuyPrice;

            if (newStock < targetStock) {
                prevBuyPrice = newBuyPrice;
                newBuyPrice = (long) Math.ceil(prevBuyPrice * (1 + 1.0/targetStock));

                prevSellPrice = newSellPrice;
                newSellPrice = (long) Math.ceil(prevSellPrice * (1 + 1.0/targetStock));
            }
        }

        long maxSell = (long) Math.max(prevBuyPrice * (1-NUMS.getPriceDiffPercent()), NUMS.getSellPriceMinimum());
        newSellPrice = Math.min(newSellPrice, maxSell);

        return new MarketTransactionData(
            cost, fixedAmount, marketData.stock() - fixedAmount, newSellPrice, newBuyPrice
        );
    }

    public static MarketTransactionData calculateSellCost(String itemID, MarketData marketData, int amount) {
        boolean isPowerup = POWS.containsKey(itemID);
        String rarityKey = isPowerup ? null : BoarUtil.findRarityKey(itemID);
        int targetStock = isPowerup
            ? POWS.get(itemID).getTargetStock()
            : RARITIES.get(rarityKey).getTargetStock();

        long cost = 0;
        long newSellPrice = marketData.sellPrice();

        for (int i=0; i<amount; i++) {
            cost += newSellPrice;
            newSellPrice = (long) Math.max(newSellPrice / (1 + 1.0/targetStock), NUMS.getSellPriceMinimum());
        }

        return new MarketTransactionData(
            cost, amount, marketData.stock() + amount, newSellPrice, marketData.buyPrice()
        );
    }
}
