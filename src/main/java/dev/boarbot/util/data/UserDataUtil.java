package dev.boarbot.util.data;

import dev.boarbot.BoarBotApp;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

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
            try (ResultSet results = statement.executeQuery()) {
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
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        }

        return 0;
    }

    public static void fixUsernames(Connection connection) throws SQLException {
        String query = """
            SELECT user_id
            FROM users u1
            WHERE (
                SELECT COUNT(*)
                FROM users u2
                WHERE u1.username = u2.username
            ) > 1;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    String userID = results.getString("user_id");

                    try {
                        BoarBotApp.getBot().getJDA().retrieveUserById(userID).queue(
                            user -> {
                                try {
                                    BoarUserFactory.getBoarUser(user);
                                } catch (SQLException exception) {
                                    Log.error(user, UserDataUtil.class, "Failed to update data", exception);
                                }
                            },
                            e -> {
                                if (e instanceof ErrorResponseException response) {
                                    if (response.getErrorResponse() == ErrorResponse.UNKNOWN_USER) {
                                        try (Connection connection1 = DataUtil.getConnection()) {
                                            BoarUser boarUser = BoarUserFactory.getBoarUser(userID);
                                            boarUser.baseQuery().deleteUser(connection1);
                                        } catch (SQLException exception) {
                                            Log.error(UserDataUtil.class, "Failed to remove deleted user", exception);
                                        }

                                        return;
                                    }
                                }

                                Log.warn(UserDataUtil.class, "Discord threw an exception while deleting users", e);
                            }
                        );
                    } catch (ErrorResponseException exception) {
                        if (exception.getErrorResponse() == ErrorResponse.UNKNOWN_USER) {
                            BoarUser boarUser = BoarUserFactory.getBoarUser(userID);
                            boarUser.baseQuery().deleteUser(connection);
                            return;
                        }

                        Log.error(UserDataUtil.class, "Discord threw an exception while fixing users", exception);
                    }
                }
            }
        }
    }

    public static void setStreakFreeze(Connection connection, boolean shouldFreeze) throws SQLException {
        String query = """
            UPDATE users
            SET streak_frozen = ?;
        """;

        try (PreparedStatement statement1 = connection.prepareStatement(query)) {
            statement1.setBoolean(1, shouldFreeze);
            statement1.executeUpdate();
        }
    }

    public static synchronized boolean isSpookyAvailable(Connection connection, String obtainType) throws SQLException {
        String query = """
            SELECT COUNT(*) < 3
            FROM collected_boars
            WHERE boar_id = 'spooky' AND original_obtain_type = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obtainType);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getBoolean(1);
                }
            }
        }

        return false;
    }
}
