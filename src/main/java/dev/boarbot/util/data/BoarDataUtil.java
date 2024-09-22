package dev.boarbot.util.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoarDataUtil {
    public static int getTotalUniques(Connection connection) throws SQLException {
        String query = """
            SELECT COUNT(*)
            FROM boars_info;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        }

        return 0;
    }

    public static int getTotalBoars(Connection connection) throws SQLException {
        String query = """
            SELECT COUNT(*)
            FROM collected_boars
            WHERE `exists` = true AND deleted = false;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        }

        return 0;
    }
}
