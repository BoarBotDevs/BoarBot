package dev.boarbot.migration;

import dev.boarbot.api.util.Configured;
import dev.boarbot.migration.guilddata.OldGuildData;
import dev.boarbot.migration.userdata.*;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.Log;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

class MigrationWriter implements Configured {
    private static final long EARLIEST_START_TIME = 1688169600000L;

    public static void writeUsers(List<OldUserData> oldUsers) {
        Log.debug(MigrationReader.class, "Writing old user data...");
        try (Connection connection = DataUtil.getConnection()) {
            writeBaseUsers(oldUsers, connection);
            writeQuestUsers(oldUsers, connection);
            writePowerupUsers(oldUsers, connection);
            writeBadgeUsers(oldUsers, connection);
            Log.debug(MigrationReader.class, "Wrote old user data");
        } catch (SQLException exception) {
            Log.error(MigrationReader.class, "Failed to migrate users to database", exception);
        }
    }

    private static void writeBaseUsers(List<OldUserData> oldUsers, Connection connection) throws SQLException {
        String addUserQuery = """
            INSERT INTO users (
                user_id,
                username,
                last_daily_timestamp,
                num_dailies,
                boar_streak,
                highest_streak,
                total_bucks,
                favorite_boar_id,
                last_boar_id,
                first_joined_timestamp,
                highest_blessings,
                notifications_on,
                notification_channel,
                powerup_attempts,
                powerup_wins,
                powerup_perfects,
                powerup_fastest_time,
                gifts_opened,
                miracles_active
            )
            VALUES (?, IFNULL(?, DEFAULT(username)), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (PreparedStatement statement = connection.prepareStatement(addUserQuery)) {
            for (OldUserData oldUser : oldUsers) {
                GeneralStatsData generalStats = oldUser.getStats().getGeneral();
                PowerupStatsData powerupStats = oldUser.getStats().getPowerups();
                PowerupData giftData = oldUser.getItemCollection().getPowerups().get("gift");
                PowerupData miracleData = oldUser.getItemCollection().getPowerups().get("miracle");

                statement.setString(1, oldUser.getUserID());
                statement.setString(2, oldUser.getUsername());
                statement.setTimestamp(3, generalStats.getLastDaily() == 0
                    ? null
                    : new Timestamp(generalStats.getLastDaily())
                );
                statement.setInt(4, generalStats.getNumDailies());
                statement.setInt(5, generalStats.getBoarStreak());
                statement.setInt(6, generalStats.getHighestStreak());
                statement.setInt(7, generalStats.getBoarScore());
                statement.setString(8, generalStats.getFavoriteBoar().isEmpty()
                    ? null
                    : generalStats.getFavoriteBoar()
                );
                statement.setString(9, generalStats.getLastBoar().isEmpty()
                    ? null
                    : generalStats.getLastBoar()
                );
                statement.setTimestamp(10, generalStats.getFirstDaily() == 0
                    ? generalStats.getLastDaily() == 0
                        ? new Timestamp(EARLIEST_START_TIME)
                        : new Timestamp(generalStats.getLastDaily())
                    : new Timestamp(generalStats.getFirstDaily())
                );
                statement.setInt(11, generalStats.getHighestMulti());
                statement.setBoolean(12, generalStats.isNotificationsOn());

                if (generalStats.getNotificationChannel() == null && generalStats.isNotificationsOn()) {
                    statement.setString(13, CONFIG.getMainConfig().getPingChannel());
                } else if (generalStats.isNotificationsOn()) {
                    statement.setString(13, generalStats.getNotificationChannel());
                } else {
                    statement.setString(13, null);
                }

                statement.setInt(14, powerupStats.getAttempts());
                statement.setInt(15, powerupStats.getAttempts());
                statement.setInt(16, powerupStats.getOneAttempts());
                statement.setInt(17, powerupStats.getFastestTime() == 0
                    ? 120000
                    : Math.max(powerupStats.getFastestTime(), 1000)
                );
                statement.setInt(18, giftData.getNumOpened());
                statement.setInt(19, miracleData.getNumActive());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void writeQuestUsers(List<OldUserData> oldUsers, Connection connection) throws SQLException {
        String updateQuery = """
            UPDATE user_quests
            SET num_completed = ?, num_full_completed = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            for (OldUserData oldUser : oldUsers) {
                QuestsStatsData questStats = oldUser.getStats().getQuests();

                statement.setInt(1, questStats.getTotalCompleted());
                statement.setInt(2, questStats.getTotalFullCompleted());
                statement.setString(3, oldUser.getUserID());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void writePowerupUsers(List<OldUserData> oldUsers, Connection connection) throws SQLException {
        String powerupQuery = """
            INSERT INTO collected_powerups (user_id, powerup_id, amount, highest_amount, amount_used)
            VALUES (?, ?, ?, ?, ?);
        """;

        String cloneQuery = """
            INSERT INTO clone_stats (user_id, rarity_id, amount)
            VALUES (?, ?, ?);
        """;

        String transmuteQuery = """
            INSERT INTO transmute_stats (user_id, rarity_id, amount)
            VALUES (?, ?, ?);
        """;

        String promptQuery = """
            INSERT INTO prompt_stats (user_id, prompt_id, average_placement, wins)
            VALUES (?, ?, ?, ?);
        """;

        try (
            PreparedStatement statement1 = connection.prepareStatement(powerupQuery);
            PreparedStatement statement2 = connection.prepareStatement(cloneQuery);
            PreparedStatement statement3 = connection.prepareStatement(transmuteQuery);
            PreparedStatement statement4 = connection.prepareStatement(promptQuery)
        ) {
            for (OldUserData oldUser : oldUsers) {
                Map<String, PowerupData> powerups = oldUser.getItemCollection().getPowerups();
                PowerupStatsData powerupStats = oldUser.getStats().getPowerups();

                for (String powerupID : powerups.keySet()) {
                    PowerupData powerup = powerups.get(powerupID);

                    if (powerupID.equals("clone")) {
                        tryWritePowRarities(powerupID, powerup, oldUser.getUserID(), statement2);
                    } else if (powerupID.equals("enhancer")) {
                        tryWritePowRarities(powerupID, powerup, oldUser.getUserID(), statement3);
                    }

                    statement1.setString(1, oldUser.getUserID());
                    statement1.setString(2, powerupID.equals("enhancer") ? "transmute" : powerupID);
                    statement1.setInt(3, powerup.getNumTotal());
                    statement1.setInt(4, powerup.getHighestTotal());
                    statement1.setInt(5, powerup.getNumUsed());

                    statement1.addBatch();
                }

                for (String promptTypeID : powerupStats.getPrompts().keySet()) {
                    for (String promptID : powerupStats.getPrompts().get(promptTypeID).keySet()) {
                        PromptStatsData promptStats = powerupStats.getPrompts().get(promptTypeID).get(promptID);

                        String fixedPromptID = switch (promptTypeID) {
                            case "emojiFind" -> promptID.substring(0, promptID.length() - 1) + "Emoji";
                            case "trivia" -> promptID.substring(0, promptID.length() - 1) + "Trivia";
                            case "fast" -> promptID + "Btn";
                            case "time" -> promptID + "Clock";
                            case "anniversary" -> promptID + "Ann";
                            default -> promptID;
                        };

                        statement4.setString(1, oldUser.getUserID());
                        statement4.setString(2, fixedPromptID);
                        statement4.setDouble(3, promptStats.getAvg() / 100);
                        statement4.setInt(4, promptStats.getAttempts());

                        statement4.addBatch();
                    }
                }
            }

            statement1.executeBatch();
            statement2.executeBatch();
            statement3.executeBatch();
            statement4.executeBatch();
        }
    }

    private static void tryWritePowRarities(
        String powerupID, PowerupData powerup, String userID, PreparedStatement statement
    ) throws SQLException {
        if (powerup.getHighestTotal() == 0) {
            return;
        }

        String[] rarities = powerupID.equals("clone")
            ? new String[] {
                "halloween",
                "christmas",
                "common",
                "uncommon",
                "rare",
                "epic",
                "legendary",
                "mythic",
                "divine",
                "immaculate"
            }
            : new String[] {"common", "uncommon", "rare", "epic", "legendary", "mythic", "divine"};

        for (int i=0; i<powerup.getRaritiesUsed().length; i++) {
            if (powerup.getRaritiesUsed()[i] == 0) {
                continue;
            }

            if (powerupID.equals("enhancer")) {
                powerup.setNumUsed(
                    powerup.getNumUsed() + RARITIES.get(rarities[i]).getChargesNeeded() * powerup.getRaritiesUsed()[i]
                );
            }

            statement.setString(1, userID);
            statement.setString(2, rarities[i]);
            statement.setInt(3, powerup.getRaritiesUsed()[i]);

            statement.addBatch();
        }
    }

    private static void writeBadgeUsers(List<OldUserData> oldUsers, Connection connection) throws SQLException {
        String badgeQuery = """
            INSERT INTO collected_badges (user_id, badge_id, badge_tier, obtained_timestamp, first_obtained_timestamp)
            VALUES (?, ?, ?, ?, ?);
        """;

        try (PreparedStatement statement = connection.prepareStatement(badgeQuery)) {
            for (OldUserData oldUser : oldUsers) {
                for (String badgeID : oldUser.getItemCollection().getBadges().keySet()) {
                    BadgeData badge = oldUser.getItemCollection().getBadges().get(badgeID);

                    if (!badge.isPossession()) {
                        continue;
                    }

                    String fixedBadgeID;

                    if (badgeID.equals("early_supporter")) {
                        fixedBadgeID = "supporter";
                    } else if (badgeID.equals("hunter")) {
                        fixedBadgeID = "researcher";
                    } else {
                        fixedBadgeID = badgeID;
                    }

                    statement.setString(1, oldUser.getUserID());
                    statement.setString(2, fixedBadgeID);

                    int badgeTier = badge.isPossession() ? 0 : -1;

                    if (fixedBadgeID.equals("athlete") && badgeTier == 0) {
                        badgeTier = 2;
                    }

                    statement.setInt(3, badgeTier);
                    statement.setTimestamp(4, new Timestamp(badge.getCurObtained()));
                    statement.setTimestamp(5, new Timestamp(badge.getFirstObtained()));

                    statement.addBatch();
                }
            }

            statement.executeBatch();
        }
    }

    public static void writeBoars(Map<String, PriorityQueue<NewBoarData>> boars) {
        Log.debug(MigrationReader.class, "Writing old boar data...");

        try (Connection connection = DataUtil.getConnection()) {
            String boarQuery = """
                INSERT INTO collected_boars (user_id, boar_id, obtained_timestamp)
                VALUES (?, ?, IFNULL(?, DEFAULT(obtained_timestamp)));
            """;

            try (PreparedStatement statement = connection.prepareStatement(boarQuery)) {
                for (String boarID : boars.keySet()) {
                    PriorityQueue<NewBoarData> newBoars = boars.get(boarID);

                    while (newBoars != null && !newBoars.isEmpty()) {
                        NewBoarData newBoar = newBoars.poll();

                        statement.setString(1, newBoar.getUserID());
                        statement.setString(2, boarID);
                        statement.setTimestamp(3, newBoar.getObtainedTime() == Long.MAX_VALUE
                            ? null
                            : new Timestamp(newBoar.getObtainedTime())
                        );

                        statement.addBatch();
                    }
                }

                statement.executeBatch();
            }

            Log.debug(MigrationReader.class, "Wrote old boar data");
        } catch (SQLException exception) {
            Log.error(MigrationReader.class, "Failed to migrate boars to database", exception);
        }
    }

    public static void writeGuilds(List<OldGuildData> oldGuilds) {
        Log.debug(MigrationReader.class, "Writing old guild data...");

        try (Connection connection = DataUtil.getConnection()) {
            String addGuildQuery = """
                INSERT INTO guilds (
                    guild_id,
                    is_skyblock_community,
                    channel_one,
                    channel_two,
                    channel_three
                )
                VALUES (?, ?, ?, ?, ?);
            """;

            try (PreparedStatement statement = connection.prepareStatement(addGuildQuery)) {
                for (OldGuildData oldGuild : oldGuilds) {
                    statement.setString(1, oldGuild.getGuildID());
                    statement.setBoolean(2, oldGuild.isSBServer());

                    for (int i=0; i<3; i++) {
                        boolean canAdd = oldGuild.getChannels().length > i;
                        statement.setString(i+3, canAdd ? oldGuild.getChannels()[i] : null);
                    }

                    statement.addBatch();
                }

                statement.executeBatch();
            }

            Log.debug(MigrationReader.class, "Wrote old guild data");
        } catch (SQLException exception) {
            Log.error(MigrationReader.class, "Failed to migrate guilds to database", exception);
        }
    }
}
