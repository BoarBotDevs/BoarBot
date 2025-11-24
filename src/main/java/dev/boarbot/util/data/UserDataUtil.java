package dev.boarbot.util.data;

import dev.boarbot.BoarBotApp;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.sql.*;
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
                                            boarUser.baseQuery().wipeUser(connection1);
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
                            boarUser.baseQuery().wipeUser(connection);
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

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, shouldFreeze);
            statement.executeUpdate();
        }
    }

    public static synchronized boolean isSpookyAvailable(Connection connection, String obtainType) throws SQLException {
        String query = """
            SELECT COUNT(*) < 5
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

    public static void removeWipeUsers(Connection connection) throws SQLException {
        String wipeUsersQuery = """
            SELECT user_id, username, unban_timestamp
            FROM users
            WHERE wipe_timestamp IS NOT NULL AND wipe_timestamp < current_timestamp(3);
        """;

        String fullWipeUsersQuery = """
            SELECT user_id
            FROM users
            WHERE first_joined_timestamp = 0 AND unban_timestamp < current_timestamp(3);
        """;

        String deleteQuery = """
            DELETE FROM users
            WHERE user_id = ?;
        """;

        String addWipeBanQuery = """
            INSERT INTO users (user_id, username, first_joined_timestamp, unban_timestamp)
            VALUES (?, ?, 0, ?);
        """;

        List<String> wipeUsers = new ArrayList<>();
        List<String> wipeUsernames = new ArrayList<>();
        List<Timestamp> unbanTimestamps = new ArrayList<>();
        List<String> fullWipeUsers = new ArrayList<>();

        try (
            PreparedStatement statement1 = connection.prepareStatement(wipeUsersQuery);
            PreparedStatement statement2 = connection.prepareStatement(fullWipeUsersQuery);
            PreparedStatement statement3 = connection.prepareStatement(deleteQuery);
            PreparedStatement statement4 = connection.prepareStatement(addWipeBanQuery)
        ) {
            try (ResultSet results = statement1.executeQuery()) {
                while (results.next()) {
                    wipeUsers.add(results.getString("user_id"));
                    wipeUsernames.add(results.getString("username"));
                    unbanTimestamps.add(results.getTimestamp("unban_timestamp"));
                }
            }

            try (ResultSet results = statement2.executeQuery()) {
                while (results.next()) {
                    fullWipeUsers.add(results.getString("user_id"));
                }
            }

            for (String userID : wipeUsers) {
                statement3.setString(1, userID);
                statement3.addBatch();
            }

            for (String userID : fullWipeUsers) {
                statement3.setString(1, userID);
                statement3.addBatch();
            }

            statement3.executeBatch();

            for (int i=0; i<wipeUsers.size(); i++) {
                statement4.setString(1, wipeUsers.get(i));
                statement4.setString(2, wipeUsernames.get(i));

                Timestamp baseTimestamp = new Timestamp(TimeUtil.getCurMilli() + TimeUtil.getOneDayMilli() * 30);

                if (unbanTimestamps.get(i) != null) {
                    Timestamp timestamp = baseTimestamp.after(unbanTimestamps.get(i))
                        ? baseTimestamp
                        : unbanTimestamps.get(i);

                    statement4.setTimestamp(3, timestamp);
                } else {
                    statement4.setTimestamp(3, baseTimestamp);
                }

                statement4.addBatch();
            }

            statement4.executeBatch();
        }
    }

    public static void resetOtherBless(Connection connection) throws SQLException {
        String query = """
            UPDATE users
            SET other_bless = 0
            WHERE other_bless != 0;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        }
    }

    public static void resetAdventBits(Connection connection) throws SQLException {
        String query = """
            UPDATE users
            SET advent_bits = 0
            WHERE advent_bits != 0;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        }
    }

    public static void refreshUniques(Connection connection) throws SQLException {
        String query = """
            UPDATE users
            SET unique_boars = 0;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        }
    }
}
