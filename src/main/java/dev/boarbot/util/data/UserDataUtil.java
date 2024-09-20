package dev.boarbot.util.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDataUtil {
    public static List<String> getNotifUserIDs(Connection connection) throws SQLException {
        List<String> notifUsers = new ArrayList<>();

        String query = """
            SELECT user_id
            FROM users
            WHERE notifications_on = true;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeQuery();

            try (ResultSet results = statement.getResultSet()) {
                while (results.next()) {
                    notifUsers.add(results.getString("user_id"));
                }
            }
        }

        return notifUsers;
    }

    public static int getTotalUsers(Connection connection) throws SQLException {
        String query = """
            SELECT COUNT(*)
            FROM users;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeQuery();

            try (ResultSet results = statement.getResultSet()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        }

        return 0;
    }
}
