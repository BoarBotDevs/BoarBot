package dev.boarbot.util.data.top;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TopDataUtil {
    private static final int MAX_ATHLETE_TIER = 2;

    public static Map<String, TopData> getBoard(TopType type, Connection connection) throws SQLException {
        return switch (type) {
            case FASTEST_POWERUP -> getUserBoard(type, false, 120000, connection);
            case GIFTS_SENT, CHARGES_USED -> getPowBoard(type, connection);
            default -> getUserBoard(type, true, 0, connection);
        };
    }

    private static Map<String, TopData> getUserBoard(
        TopType type, boolean isDesc, long defaultVal, Connection connection
    ) throws SQLException {
        Map<String, TopData> board = new LinkedHashMap<>();

        String query = """
            SELECT username, %s
            FROM users
            WHERE %s
            ORDER BY %s
            %s;
        """;

        if (isDesc) {
            query = query.formatted(type.toString(), type + " > " + defaultVal, type.toString(), "DESC");
        } else {
            query = query.formatted(type.toString(), type + " < " + defaultVal, type.toString(), "ASC");
        }

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                int count = 0;
                while (resultSet.next()) {
                    board.put(resultSet.getString(1), new TopData(resultSet.getLong(2), count));
                    count++;
                }
            }
        }

        return board;
    }

    private static Map<String, TopData> getPowBoard(TopType type, Connection connection) throws SQLException {
        Map<String, TopData> board = new LinkedHashMap<>();

        String query = """
            SELECT username, amount_used
            FROM collected_powerups, users
            WHERE powerup_id = ? AND collected_powerups.user_id = users.user_id AND amount_used > 0
            ORDER BY amount_used
            DESC;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, type.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                int count = 0;
                while (resultSet.next()) {
                    board.put(resultSet.getString(1), new TopData(resultSet.getLong(2), count));
                    count++;
                }
            }
        }

        return board;
    }

    public static void setAthleteBadges(
        Set<String> firstUsernames, Set<String> secondUsernames, Set<String> thirdUsernames, Connection connection
    ) throws SQLException {
        List<Set<String>> usernameSets = new ArrayList<>();

        usernameSets.add(firstUsernames);
        usernameSets.add(secondUsernames);
        usernameSets.add(thirdUsernames);

        List<String> setStrs = getSetStrs(usernameSets);
        String allSetStrs = String.join(",", setStrs);

        String deleteQuery = """
            DELETE FROM collected_badges
            WHERE
                badge_id = 'athlete' AND
                collected_badges.user_id NOT IN (
                    SELECT users.user_id
                    FROM users
                    WHERE users.username IN (%s)
                );
        """.formatted(allSetStrs);

        String updateQuery = """
            UPDATE collected_badges
            SET badge_tier = ?
            WHERE
                badge_id = 'athlete' AND
                collected_badges.user_id IN (
                    SELECT user_id
                    FROM users
                    WHERE username IN (%s)
                );
        """;

        String insertQuery = """
            INSERT INTO collected_badges (user_id, badge_id, badge_tier)
            SELECT user_id, 'athlete', ?
            FROM users
            WHERE username = ? AND NOT EXISTS (
                SELECT 1
                FROM collected_badges
                WHERE badge_id = 'athlete' AND user_id = users.user_id
            );
        """;

        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.executeUpdate();
        }

        int tier = MAX_ATHLETE_TIER;
        for (Set<String> usernameSet : usernameSets) {
            String setStr = setStrs.get(Math.abs(tier-MAX_ATHLETE_TIER));

            try (
                PreparedStatement statement1 = connection.prepareStatement(updateQuery.formatted(setStr));
                PreparedStatement statement2 = connection.prepareStatement(insertQuery.formatted(setStr))
            ) {
                statement1.setInt(1, tier);
                statement1.executeUpdate();

                for (String username : usernameSet) {
                    statement2.setInt(1, tier);
                    statement2.setString(2, username);
                    statement2.addBatch();
                }

                statement2.executeBatch();
            }

            tier--;
        }
    }

    private static List<String> getSetStrs(List<Set<String>> usernameSets) {
        StringJoiner joiner = new StringJoiner("','", "'", "'");
        List<String> setStrs = new ArrayList<>();

        for (Set<String> usernameSet : usernameSets) {
            for (String username : usernameSet) {
                joiner.add(username);
            }

            if (joiner.toString().isEmpty()) {
                joiner.add("");
            }

            setStrs.add(joiner.toString());
            joiner = new StringJoiner("','", "'", "'");
        }

        return setStrs;
    }
}
