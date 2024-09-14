package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.time.TimeUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoarQueries implements Configured {
    private final BoarUser boarUser;

    public BoarQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    public void addBoars(
        List<String> boarIDs,
        Connection connection,
        BoarObtainType obtainType,
        List<Integer> bucksGotten,
        List<Integer> boarEditions
    ) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        List<String> newBoarIDs = new ArrayList<>();

        if (this.boarUser.isFirstDaily()) {
            this.boarUser.powQuery().addPowerup(connection, "miracle", 5);
            this.boarUser.powQuery().addPowerup(connection, "gift", 1);
        }
        this.boarUser.setFirstDaily(false);

        for (String boarID : boarIDs) {
            String boarAddQuery = """
                INSERT INTO collected_boars (user_id, boar_id, original_obtain_type)
                VALUES (?, ?, ?)
                RETURNING edition, bucks_gotten;
            """;
            int curEdition;

            try (PreparedStatement boarAddStatement = connection.prepareStatement(boarAddQuery)) {
                boarAddStatement.setString(1, this.boarUser.getUserID());
                boarAddStatement.setString(2, boarID);
                boarAddStatement.setString(3, obtainType.toString());

                try (ResultSet results = boarAddStatement.executeQuery()) {
                    if (results.next()) {
                        curEdition = results.getInt("edition");

                        newBoarIDs.add(boarID);
                        boarEditions.add(curEdition);
                        bucksGotten.add(results.getInt("bucks_gotten"));

                        String rarityKey = BoarUtil.findRarityKey(boarID);

                        if (curEdition == 1 && RARITIES.get(rarityKey).isGivesFirstBoar()) {
                            this.addFirstBoar(newBoarIDs, connection, bucksGotten, boarEditions);
                        }
                    }
                }
            }
        }

        boarIDs.clear();
        boarIDs.addAll(newBoarIDs);
    }

    public boolean hasBoar(String boarID, Connection connection) throws SQLException {
        String query = """
            SELECT boar_id
            FROM collected_boars
            WHERE boar_id = ? AND user_id = ? AND `exists` = 1 AND deleted = 0;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, boarID);
            statement.setString(2, this.boarUser.getUserID());
            return statement.executeQuery().next();
        }
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
    }

    private void addFirstBoar(
        List<String> newBoarIDs,
        Connection connection,
        List<Integer> bucksGotten,
        List<Integer> boarEditions
    ) throws SQLException {
        String insertFirstQuery = """
            INSERT INTO collected_boars (user_id, boar_id, original_obtain_type)
            VALUES (?, ?, ?)
            RETURNING edition;
        """;
        String firstBoarID = CONFIG.getMainConfig().getFirstBoarID();

        if (!BOARS.containsKey(firstBoarID)) {
            return;
        }

        try (PreparedStatement insertFirstStatement = connection.prepareStatement(insertFirstQuery)) {
            insertFirstStatement.setString(1, this.boarUser.getUserID());
            insertFirstStatement.setString(2, firstBoarID);
            insertFirstStatement.setString(3, BoarObtainType.OTHER.toString());

            try (ResultSet results = insertFirstStatement.executeQuery()) {
                if (results.next()) {
                    newBoarIDs.add(firstBoarID);
                    boarEditions.add(results.getInt("edition"));
                    bucksGotten.add(0);
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
