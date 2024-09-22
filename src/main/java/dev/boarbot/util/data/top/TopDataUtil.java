package dev.boarbot.util.data.top;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

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
}
