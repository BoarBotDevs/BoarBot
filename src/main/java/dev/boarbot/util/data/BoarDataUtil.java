package dev.boarbot.util.data;

import dev.boarbot.api.util.Configured;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoarDataUtil implements Configured {
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

    public static boolean boarExists(String boarID, Connection connection) throws SQLException {
        String query = """
            SELECT 1
            FROM collected_boars
            WHERE boar_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, boarID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return true;
                }
            }
        }

        return false;
    }
}
