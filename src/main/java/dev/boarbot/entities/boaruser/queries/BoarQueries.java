package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.util.boar.BoarTag;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.market.MarketDataUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;

import java.sql.*;
import java.util.*;

public class BoarQueries implements Configured {
    private final BoarUser boarUser;

    public BoarQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    public void addBoars(
        List<String> boarIDs,
        Connection connection,
        String boarTag,
        List<Integer> bucksGotten,
        List<Integer> boarEditions,
        Set<String> firstBoarIDs
    ) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        List<String> newBoarIDs = new ArrayList<>();

        if (this.boarUser.isFirstDaily()) {
            Log.debug(this.boarUser.getUser(), this.getClass(), "Giving first daily bonus");
            this.boarUser.powQuery().addPowerup(connection, "miracle", 5);
            this.boarUser.powQuery().addPowerup(connection, "gift", 1);
        }
        this.boarUser.setFirstDaily(false);

        int boarIndex = -1;
        for (String boarID : boarIDs) {
            boarIndex++;

            String boarAddQuery = """
                INSERT INTO collected_boars (user_id, boar_id, tag)
                VALUES (?, ?, ?)
                RETURNING edition, bucks_gotten;
            """;

            String isFirstQuery = """
                SELECT COUNT(*) = 1
                FROM collected_boars
                WHERE user_id = ? AND boar_id = ?;
            """;

            String updateFirstJoined = """
                UPDATE users
                SET first_joined_timestamp = current_timestamp(3)
                WHERE first_joined_timestamp = '0000-00-00 00:00:00' AND user_id = ?;
            """;

            int curEdition;

            try (
                PreparedStatement boarAddStatement = connection.prepareStatement(boarAddQuery);
                PreparedStatement isFirstStatement = connection.prepareStatement(isFirstQuery);
                PreparedStatement updateFirstStatement = connection.prepareStatement(updateFirstJoined)
            ) {
                boarAddStatement.setString(1, this.boarUser.getUserID());
                boarAddStatement.setString(2, boarID);
                boarAddStatement.setString(3, boarTag.equals("DAILY") && boarIndex > 0
                    ? BoarTag.EXTRA.toString()
                    : boarTag
                );

                isFirstStatement.setString(1, this.boarUser.getUserID());
                isFirstStatement.setString(2, boarID);

                try (
                    ResultSet results1 = boarAddStatement.executeQuery();
                    ResultSet results2 = isFirstStatement.executeQuery()
                ) {
                    if (results1.next()) {
                        curEdition = results1.getInt("edition");

                        newBoarIDs.add(boarID);
                        boarEditions.add(curEdition);
                        bucksGotten.add(results1.getInt("bucks_gotten"));

                        String rarityKey = BoarUtil.findRarityKey(boarID);

                        if (curEdition == 1 && RARITIES.get(rarityKey).isGivesFirstBoar()) {
                            this.addFirstBoar(newBoarIDs, connection, bucksGotten, boarEditions, firstBoarIDs);
                        }

                        boolean addToMarket = RARITIES.get(rarityKey).isMarketable() &&
                            !MarketInteractive.cachedMarketData.containsKey(boarID);

                        if (addToMarket) {
                            MarketDataUtil.addNewItem(boarID);
                        }
                    }

                    if (results2.next() && results2.getBoolean(1)) {
                        firstBoarIDs.add(boarID);
                    }
                }

                updateFirstStatement.setString(1, this.boarUser.getUserID());
                updateFirstStatement.executeUpdate();
            }
        }

        if (!boarTag.equals("DAILY")) {
            this.boarUser.baseQuery().updateHighestBlessings(connection);
        }

        boarIDs.clear();
        boarIDs.addAll(newBoarIDs);

        for (int i=0; i<boarIDs.size(); i++) {
            String boarID = boarIDs.get(i);
            int bucks = bucksGotten.get(i);
            long edition = boarEditions.get(i);

            Log.info(
                this.boarUser.getUser(), this.getClass(), "Obtained %s ($%,d, #%,d)".formatted(boarID, bucks, edition)
            );
        }
    }

    public int getBoarAmount(String boarID, Connection connection) throws SQLException {
        int amount = 0;

        String query = """
            SELECT COUNT(*)
            FROM collected_boars
            WHERE boar_id = ? AND user_id = ? AND `exists` = true AND deleted = false;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, boarID);
            statement.setString(2, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    amount = results.getInt(1);
                }
            }
        }

        return amount;
    }

    public boolean hasBoar(String boarID, Connection connection) throws SQLException {
        return this.getBoarAmount(boarID, connection) > 0;
    }

    public boolean hasYearlySpooky(Connection connection) throws SQLException {
        StringJoiner joiner = new StringJoiner("','", "'", "'");

        for (int i=0; i<STRS.getSpookGuessStrs().length; i++) {
            joiner.add("SPOOK_%d_%d".formatted(i+1, TimeUtil.getYear()));
        }

        String query = """
            SELECT 1
            FROM collected_boars
            WHERE user_id = ? AND boar_id = 'spooky' AND tag IN (%s);
        """.formatted(joiner.toString());

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasBoarWithTag(Connection connection, String boarTag) throws SQLException {
        String query = """
            SELECT 1
            FROM collected_boars
            WHERE user_id = ? AND tag = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.setString(2, boarTag);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    public void removeBoar(String boarID, Connection connection) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE collected_boars
            SET deleted = 1
            WHERE boar_id = ? AND user_id = ? AND `exists` = 1 AND deleted = 0
            ORDER BY edition DESC
            LIMIT 1;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, boarID);
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        Log.info(this.boarUser.getUser(), this.getClass(), "Removed %s from collection".formatted(boarID));
    }

    private void addFirstBoar(
        List<String> newBoarIDs,
        Connection connection,
        List<Integer> bucksGotten,
        List<Integer> boarEditions,
        Set<String> firstBoarIDs
    ) throws SQLException {
        String insertFirstQuery = """
            INSERT INTO collected_boars (user_id, boar_id, tag)
            VALUES (?, ?, ?)
            RETURNING edition, (
                SELECT COUNT(*)
                WHERE user_id = ? AND boar_id = ?
            );
        """;

        String isFirstQuery = """
            SELECT COUNT(*) = 1
            FROM collected_boars
            WHERE user_id = ? AND boar_id = ?;
        """;

        String firstBoarID = CONFIG.getMainConfig().getFirstBoarID();

        if (!BOARS.containsKey(firstBoarID)) {
            return;
        }

        try (
            PreparedStatement insertFirstStatement = connection.prepareStatement(insertFirstQuery);
            PreparedStatement isFirstStatement = connection.prepareStatement(isFirstQuery)
        ) {
            insertFirstStatement.setString(1, this.boarUser.getUserID());
            insertFirstStatement.setString(2, firstBoarID);
            insertFirstStatement.setString(3, BoarTag.EXTRA.toString());
            insertFirstStatement.setString(4, this.boarUser.getUserID());
            insertFirstStatement.setString(5, firstBoarID);

            isFirstStatement.setString(1, this.boarUser.getUserID());
            isFirstStatement.setString(2, firstBoarID);

            try (
                ResultSet results1 = insertFirstStatement.executeQuery();
                ResultSet results2 = isFirstStatement.executeQuery()
            ) {
                if (results1.next()) {
                    newBoarIDs.add(firstBoarID);
                    boarEditions.add(results1.getInt("edition"));
                    bucksGotten.add(0);
                }

                if (results2.next() && results2.getBoolean(1)) {
                    firstBoarIDs.add(firstBoarID);
                }
            }
        }
    }

    public boolean canUseDaily(Connection connection) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);

        boolean canUseDaily = false;
        String query = """
            SELECT last_daily_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    Timestamp lastDailyTimestamp = results.getTimestamp("last_daily_timestamp");

                    canUseDaily = lastDailyTimestamp == null ||
                        lastDailyTimestamp.getTime() < TimeUtil.getLastDailyResetMilli();

                    this.boarUser.setFirstDaily(lastDailyTimestamp == null);
                }
            }
        }

        return canUseDaily;
    }
}
