package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;

import java.sql.*;

public class BaseQueries implements Configured {
    private final BoarUser boarUser;

    public BaseQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    void addUser(Connection connection) throws SQLException {
        if (this.userExists(connection)) {
            return;
        }

        String query = """
            INSERT INTO users (user_id, username) VALUES (?, ?)
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.setString(2, this.boarUser.getUser().getName());
            statement.execute();
        }

        Log.info(this.boarUser.getUser(), this.getClass(), "New user!");
    }

    public boolean userExists(Connection connection) throws SQLException {
        String query = """
            SELECT user_id
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    public void updateUser(Connection connection, boolean syncBypass) throws SQLException {
        if (!syncBypass) {
            this.boarUser.forceSynchronized();
        }

        String query = """
            SELECT username, last_daily_timestamp, last_streak_fix, first_joined_timestamp, boar_streak, streak_frozen
            FROM users
            WHERE user_id = ?;
        """;

        boolean usernameChanged;
        boolean streakFrozen;
        long lastDailyLong = 0;
        long lastStreakLong = 0;
        long firstJoinedLong = 0;
        int boarStreak;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return;
                }

                streakFrozen = results.getBoolean("streak_frozen");
                usernameChanged = this.boarUser.getUser(true) != null &&
                    !results.getString("username").equals(this.boarUser.getUser().getName());

                Timestamp lastDailyTimestamp = results.getTimestamp("last_daily_timestamp");
                Timestamp lastStreakFixTimestamp = results.getTimestamp("last_streak_fix");
                Timestamp firstJoinedTimestamp = results.getTimestamp("first_joined_timestamp");

                if (lastDailyTimestamp != null) {
                    lastDailyLong = lastDailyTimestamp.getTime();
                }

                if (lastStreakFixTimestamp != null) {
                    lastStreakLong = lastStreakFixTimestamp.getTime();
                }

                if (firstJoinedTimestamp != null) {
                    firstJoinedLong = firstJoinedTimestamp.getTime();
                }

                boarStreak = results.getInt("boar_streak");
            }
        }

        if (usernameChanged) {
            String updateUsernameQuery = """
                UPDATE users
                SET username = ?
                WHERE user_id = ?;
            """;

            try (PreparedStatement statement = connection.prepareStatement(updateUsernameQuery)) {
                statement.setString(1, this.boarUser.getUser().getName());
                statement.setString(2, this.boarUser.getUserID());
                statement.executeUpdate();
            }
        }

        if (streakFrozen) {
            return;
        }

        int newBoarStreak = boarStreak;
        long timeToReach = Math.max(Math.max(lastDailyLong, lastStreakLong), firstJoinedLong);
        long curTimeCheck = TimeUtil.getLastDailyResetMilli() - TimeUtil.getOneDayMilli();
        int curRemove = 7;
        int curDailiesMissed = 0;

        while (timeToReach < curTimeCheck) {
            newBoarStreak = Math.max(newBoarStreak - curRemove, 0);
            curTimeCheck -= TimeUtil.getOneDayMilli();

            if (newBoarStreak != 0) {
                curRemove *= 2;
            }

            curDailiesMissed++;
        }

        if (curDailiesMissed == 0) {
            return;
        }

        String updateQuery = """
            UPDATE users
            SET boar_streak = ?, num_dailies_missed = num_dailies_missed + ?, last_streak_fix = ?
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, newBoarStreak);
            statement.setInt(2, curDailiesMissed);
            statement.setTimestamp(3, new Timestamp(TimeUtil.getLastDailyResetMilli()-1));
            statement.setString(4, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        Log.debug(
            this.boarUser.getUser(), this.getClass(), "Decreased streak %,d -> %,d".formatted(boarStreak, newBoarStreak)
        );
    }

    public long getLastChanged(Connection connection) throws SQLException {
        long lastChangedTimestamp = TimeUtil.getCurMilli();
        String query = """
            SELECT last_changed_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    lastChangedTimestamp = results.getTimestamp("last_changed_timestamp").getTime();
                }
            }
        }

        return lastChangedTimestamp;
    }

    public long getBucks(Connection connection) throws SQLException {
        long bucks = 0;

        String query = """
            SELECT total_bucks
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    bucks = results.getLong("total_bucks");
                }
            }
        }

        return bucks;
    }

    public void giveBucks(Connection connection, long amount) throws SQLException {
        this.addUser(connection);
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE users
            SET total_bucks = total_bucks + ?
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, amount);
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        if (amount > 100) {
            Log.info(this.boarUser.getUser(), this.getClass(), "Obtained +$%,d".formatted(amount));
        } else {
            Log.debug(this.boarUser.getUser(), this.getClass(), "Obtained +$%,d".formatted(amount));
        }
    }

    public void useBucks(Connection connection, long amount) throws SQLException {
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE users
            SET total_bucks = total_bucks - ?
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, amount);
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public void setNotifications(Connection connection, String channelID) throws SQLException {
        this.addUser(connection);

        String query = """
            UPDATE users
            SET notifications_on = ?, notification_channel = ?
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, channelID != null);
            statement.setString(2, channelID);
            statement.setString(3, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        Log.debug(this.boarUser.getUser(), this.getClass(), "Notifications enabled in channel " + channelID);
    }

    public boolean getNotificationStatus(Connection connection) throws SQLException {
        String query = """
            SELECT notifications_on
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getBoolean("notifications_on");
                }
            }
        }

        return false;
    }

    public String getNotificationChannel(Connection connection) throws SQLException {
        String query = """
            SELECT notification_channel
            FROM users
            WHERE user_id = ? AND notifications_on = true;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getString("notification_channel");
                }
            }
        }

        return null;
    }

    public int getStreak(Connection connection) throws SQLException {
        String query = """
            SELECT boar_streak
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt("boar_streak");
                }
            }
        }

        return 0;
    }

    public long getBlessings(Connection connection) throws SQLException {
        return this.getBlessings(connection, 0);
    }

    public long getBlessings(Connection connection, int extraActive) throws SQLException {
        long blessings = 0;

        String blessingsQuery = """
            SELECT blessings, miracles_active
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement blessingsStatement = connection.prepareStatement(blessingsQuery)) {
            blessingsStatement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = blessingsStatement.executeQuery()) {
                if (results.next()) {
                    int miraclesActive = results.getInt("miracles_active");
                    blessings = results.getLong("blessings");
                    int miracleIncreaseMax = NUMS.getMiracleIncreaseMax();

                    int activesLeft = miraclesActive+extraActive;
                    for (; activesLeft>0; activesLeft--) {
                        long amountToAdd = (long) Math.min(Math.ceil(blessings * 0.1), miracleIncreaseMax);

                        if (amountToAdd == NUMS.getMiracleIncreaseMax()) {
                            break;
                        }

                        blessings += amountToAdd;
                    }

                    blessings += (long) activesLeft * miracleIncreaseMax;
                }
            }
        }

        return blessings;
    }

    public void giveBadge(Connection connection, String badgeID, int tier) throws SQLException {
        String insertQuery = """
            INSERT INTO collected_badges (user_id, badge_id, first_obtained_timestamp)
            SELECT ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM collected_badges
                WHERE badge_id = ? AND user_id = ?
            );
        """;

        String updateQuery = """
            UPDATE collected_badges
            SET badge_tier = ?, obtained_timestamp = ?
            WHERE badge_id = ? AND user_id = ? AND badge_tier != ?;
        """;

        try (
            PreparedStatement statement1 = connection.prepareStatement(insertQuery);
            PreparedStatement statement2 = connection.prepareStatement(updateQuery)
        ) {
            statement1.setString(1, this.boarUser.getUserID());
            statement1.setString(2, badgeID);
            statement1.setTimestamp(3, new Timestamp(TimeUtil.getCurMilli()));
            statement1.setString(4, badgeID);
            statement1.setString(5, this.boarUser.getUserID());
            statement1.executeUpdate();

            statement2.setInt(1, tier);
            statement2.setTimestamp(2, new Timestamp(TimeUtil.getCurMilli()));
            statement2.setString(3, badgeID);
            statement2.setString(4, this.boarUser.getUserID());
            statement2.setInt(5, tier);
            statement2.executeUpdate();
        }
    }

    public void deleteUser(Connection connection) throws SQLException {
        String deleteQuery = """
            DELETE FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement1 = connection.prepareStatement(deleteQuery)) {
            statement1.setString(1, this.boarUser.getUserID());
            statement1.executeUpdate();
        }
    }

    public void setBanDuration(Connection connection, long duration) throws SQLException {
        String updateQuery = """
            UPDATE users
            SET unban_timestamp = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setTimestamp(1, new Timestamp(TimeUtil.getCurMilli() + duration * 1000 * 60 * 60));
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public long getBannedTime(Connection connection) throws SQLException {
        long bannedTime = 0;

        String query = """
            SELECT unban_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    Timestamp unbanTimestamp = results.getTimestamp("unban_timestamp");
                    bannedTime = unbanTimestamp != null
                        ? unbanTimestamp.getTime()
                        : 0;
                }
            }
        }

        return bannedTime;
    }
}
