package dev.boarbot.util.data.top;

import java.sql.*;
import java.util.*;

public class TopDataUtil {
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

        usernameSets.add(thirdUsernames);
        usernameSets.add(secondUsernames);
        usernameSets.add(firstUsernames);

        List<String> setStrs = getSetStrs(usernameSets);
        String allSetStrs = String.join(",", setStrs);

        String deleteQuery = """
            UPDATE collected_badges
            SET badge_tier = -1, update_user = false
            WHERE
                badge_id = 'athlete' AND
                collected_badges.user_id NOT IN (
                    SELECT users.user_id
                    FROM users
                    WHERE users.username IN (%s)
                );
        """.formatted(allSetStrs);

        String insertQuery = """
            INSERT INTO collected_badges (user_id, badge_id, first_obtained_timestamp, update_user)
            SELECT user_id, 'athlete', current_timestamp(3), false
            FROM users
            WHERE username = ? AND NOT EXISTS (
                SELECT 1
                FROM collected_badges
                WHERE badge_id = 'athlete' AND user_id = users.user_id
            )
            LIMIT 1;
        """;

        String updateQuery = """
            UPDATE collected_badges
            SET badge_tier = ?, obtained_timestamp = current_timestamp(3), update_user = false
            WHERE
                badge_id = 'athlete' AND
                badge_tier != ? AND
                collected_badges.user_id IN (
                    SELECT user_id
                    FROM users
                    WHERE username IN (%s)
                );
        """;

        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.executeUpdate();
        }

        int tier = 0;
        for (Set<String> usernameSet : usernameSets) {
            String setStr = setStrs.get(tier);

            try (
                PreparedStatement statement1 = connection.prepareStatement(insertQuery);
                PreparedStatement statement2 = connection.prepareStatement(updateQuery.formatted(setStr))
            ) {
                for (String username : usernameSet) {
                    statement1.setString(1, username);
                    statement1.addBatch();
                }

                statement1.executeBatch();

                statement2.setInt(1, tier);
                statement2.setInt(2, tier);
                statement2.executeUpdate();
            }

            tier++;
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
