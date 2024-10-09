package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.*;
import dev.boarbot.interactives.boar.megamenu.SortType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MegaMenuQueries implements Configured {
    private final BoarUser boarUser;

    public MegaMenuQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    public String getFavoriteID(Connection connection) throws SQLException {
        String favoriteID = null;
        String query = """
            SELECT favorite_boar_id
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    favoriteID = results.getString("favorite_boar_id");
                }
            }
        }

        return favoriteID;
    }

    public void setFavoriteID(Connection connection, String id) throws SQLException {
        String query = """
            UPDATE users
            SET favorite_boar_id = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public int getFilterBits(Connection connection) throws SQLException {
        int filterBits = 1;
        String query = """
            SELECT filter_bits
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    filterBits = results.getInt("filter_bits");
                }
            }
        }

        return filterBits;
    }

    public void setFilterBits(Connection connection, int filterBits) throws SQLException {
        String query = """
            UPDATE users
            SET filter_bits = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, filterBits);
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public SortType getSortVal(Connection connection) throws SQLException {
        SortType sortVal = SortType.RARITY_D;
        String query = """
            SELECT sort_value
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    sortVal = SortType.values()[results.getInt("sort_value")];
                }
            }
        }

        return sortVal;
    }

    public void setSortVal(Connection connection, SortType sortVal) throws SQLException {
        String query = """
            UPDATE users
            SET sort_value = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, sortVal.ordinal());
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public Map<String, BoarInfo> getOwnedBoarInfo(Connection connection) throws SQLException {
        Map<String, BoarInfo> boarInfo = new HashMap<>();

        String firstQuery = """
            SELECT
                collected_boars.boar_id,
                COUNT(*) AS amount,
                rarity_id
            FROM collected_boars, boars_info
            WHERE
                user_id = ? AND
                collected_boars.boar_id = boars_info.boar_id AND
                collected_boars.`exists` = true AND
                collected_boars.deleted = false
            GROUP BY boar_id;
        """;

        String secondQuery = """
            SELECT
                boar_id,
                edition,
                obtained_timestamp
            FROM collected_boars
            WHERE
                user_id = ? AND
                `exists` = true AND
                deleted = false;
        """;

        try (PreparedStatement statement = connection.prepareStatement(firstQuery)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    boarInfo.put(results.getString("boar_id"), new BoarInfo(
                        results.getString("rarity_id")
                    ));
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(secondQuery)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    boarInfo.get(results.getString("boar_id")).addEdition(
                        results.getLong("edition"), results.getTimestamp("obtained_timestamp").getTime()
                    );
                }
            }
        }

        boarInfo = boarInfo.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new
            ));

        return boarInfo;
    }

    public ProfileData getProfileData(Connection connection) throws SQLException {
        ProfileData profileData = new ProfileData();
        String query = """
            SELECT
                last_boar_id,
                total_bucks,
                total_boars,
                num_dailies,
                last_daily_timestamp,
                unique_boars,
                num_skyblock,
                boar_streak,
                streak_bless,
                quest_bless,
                unique_bless,
                other_bless,
                miracles_active
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    profileData = new ProfileData(
                        results.getString("last_boar_id"),
                        results.getLong("total_bucks"),
                        results.getLong("total_boars"),
                        results.getInt("num_dailies"),
                        results.getTimestamp("last_daily_timestamp"),
                        results.getInt("unique_boars"),
                        results.getInt("num_skyblock"),
                        results.getInt("boar_streak"),
                        this.boarUser.baseQuery().getBlessings(connection),
                        results.getInt("streak_bless"),
                        results.getInt("unique_bless"),
                        results.getInt("quest_bless"),
                        results.getInt("other_bless"),
                        results.getInt("miracles_active") > 0
                    );
                }
            }
        }

        return profileData;
    }

    public StatsData getStatsData(Connection connection) throws SQLException {
        StatsData statsData = new StatsData();

        String mainQuery = """
            SELECT
                *,
                COALESCE((
                    SELECT SUM(average_placement) / GREATEST(COUNT(average_placement), 1)
                    FROM prompt_stats
                    WHERE user_id = ?
                ), -1) AS powerup_average_placement,
                (
                    SELECT boar_id
                    FROM collected_boars
                    WHERE
                        collected_boars.user_id = ? AND
                        collected_boars.original_obtain_type = 'TRANSMUTE' AND
                        collected_boars.exists = true AND
                        collected_boars.deleted = false
                    ORDER BY obtained_timestamp DESC LIMIT 1
                ) AS last_transmute,
                (
                    SELECT boar_id
                    FROM collected_boars
                    WHERE
                        collected_boars.user_id = ? AND
                        collected_boars.original_obtain_type = 'CLONE' AND
                        collected_boars.exists = true AND
                        collected_boars.deleted = false
                    ORDER BY obtained_timestamp DESC LIMIT 1
                ) AS last_clone
            FROM users
            WHERE user_id = ?;
        """;

        String questQuery = """
            SELECT
                num_completed,
                num_full_completed,
                fastest_full_millis,
                auto_claim,
                easy_completed,
                medium_completed,
                hard_completed,
                very_hard_completed
            FROM user_quests
            WHERE user_id = ?
        """;

        String promptQuery = """
            SELECT prompt_id
            FROM prompt_stats
            WHERE user_id = ?
            ORDER BY average_placement DESC LIMIT 3;
        """;

        String powAmtsQuery = """
            SELECT powerup_id, amount, highest_amount, amount_used
            FROM collected_powerups
            WHERE user_id = ?;
        """;

        String transmuteQuery = """
            SELECT rarity_id, amount
            FROM transmute_stats
            WHERE user_id = ?;
        """;

        String cloneQuery = """
            SELECT rarity_id, amount
            FROM clone_stats
            WHERE user_id = ?;
        """;

        try (
            PreparedStatement statement1 = connection.prepareStatement(mainQuery);
            PreparedStatement statement2 = connection.prepareStatement(questQuery);
            PreparedStatement statement3 = connection.prepareStatement(promptQuery);
            PreparedStatement statement4 = connection.prepareStatement(powAmtsQuery);
            PreparedStatement statement5 = connection.prepareStatement(transmuteQuery);
            PreparedStatement statement6 = connection.prepareStatement(cloneQuery)
        ) {
            statement1.setString(1, this.boarUser.getUserID());
            statement1.setString(2, this.boarUser.getUserID());
            statement1.setString(3, this.boarUser.getUserID());
            statement1.setString(4, this.boarUser.getUserID());

            statement2.setString(1, this.boarUser.getUserID());

            statement3.setString(1, this.boarUser.getUserID());

            statement4.setString(1, this.boarUser.getUserID());

            statement5.setString(1, this.boarUser.getUserID());

            statement6.setString(1, this.boarUser.getUserID());

            try (
                ResultSet results1 = statement1.executeQuery();
                ResultSet results2 = statement2.executeQuery();
                ResultSet results3 = statement3.executeQuery();
                ResultSet results4 = statement4.executeQuery();
                ResultSet results5 = statement5.executeQuery();
                ResultSet results6 = statement6.executeQuery()
            ) {
                List<String> topPrompts = new ArrayList<>();

                Map<String, Integer> powAmts = new HashMap<>();
                Map<String, Integer> powHighestAmts = new HashMap<>();
                Map<String, Integer> powUsed = new HashMap<>();

                Map<String, Integer> transmuteRarities = new HashMap<>();
                Map<String, Integer> cloneRarities = new HashMap<>();

                while (results3.next()) {
                    topPrompts.add(results3.getString("prompt_id"));
                }

                while (results4.next()) {
                    String powerupID = results4.getString("powerup_id");

                    powAmts.put(powerupID, results4.getInt("amount"));
                    powHighestAmts.put(powerupID, results4.getInt("highest_amount"));
                    powUsed.put(powerupID, results4.getInt("amount_used"));
                }

                while (results5.next()) {
                    transmuteRarities.put(results5.getString("rarity_id"), results5.getInt("amount"));
                }

                while (results6.next()) {
                    cloneRarities.put(results6.getString("rarity_id"), results6.getInt("amount"));
                }

                if (results1.next() && results2.next()) {
                    statsData = new StatsData(
                        results1.getLong("total_bucks"),
                        results1.getLong("highest_bucks"),
                        results1.getInt("num_dailies"),
                        results1.getInt("num_dailies_missed"),
                        results1.getTimestamp("last_daily_timestamp"),
                        results1.getString("last_boar_id"),
                        results1.getString("favorite_boar_id"),
                        results1.getLong("total_boars"),
                        results1.getLong("highest_boars"),
                        results1.getInt("unique_boars"),
                        results1.getInt("highest_unique_boars"),
                        results1.getInt("boar_streak"),
                        results1.getInt("highest_streak"),
                        results1.getBoolean("notifications_on"),
                        this.boarUser.baseQuery().getBlessings(connection),
                        results1.getInt("highest_blessings"),
                        results1.getInt("streak_bless"),
                        results1.getInt("highest_streak_bless"),
                        results1.getInt("quest_bless"),
                        results1.getInt("highest_quest_bless"),
                        results1.getInt("unique_bless"),
                        results1.getInt("highest_unique_bless"),
                        results1.getInt("other_bless"),
                        results1.getInt("highest_other_bless"),
                        results1.getInt("powerup_attempts"),
                        results1.getInt("powerup_wins"),
                        results1.getInt("powerup_perfects"),
                        results1.getInt("powerup_fastest_time"),
                        results1.getDouble("powerup_average_placement"),
                        topPrompts,
                        powAmts,
                        powHighestAmts,
                        powUsed,
                        results1.getInt("miracles_active"),
                        results1.getInt("miracle_rolls"),
                        results1.getInt("highest_miracles_active"),
                        results1.getInt("miracle_best_bucks"),
                        results1.getString("miracle_best_rarity"),
                        results1.getString("last_transmute"),
                        transmuteRarities,
                        results1.getString("last_clone"),
                        cloneRarities,
                        results1.getInt("gift_handicap"),
                        results1.getInt("gifts_opened"),
                        results1.getInt("gift_fastest"),
                        results1.getInt("gift_best_bucks"),
                        results1.getString("gift_best_rarity"),
                        results2.getInt("num_completed"),
                        results2.getInt("num_full_completed"),
                        results2.getLong("fastest_full_millis"),
                        results2.getBoolean("auto_claim"),
                        results2.getInt("easy_completed"),
                        results2.getInt("medium_completed"),
                        results2.getInt("hard_completed"),
                        results2.getInt("very_hard_completed")
                    );
                }
            }
        }

        return statsData;
    }

    public PowerupsData getPowerupsData(Connection connection) throws SQLException {
        String powQuery = """
            SELECT powerup_id, amount
            FROM collected_powerups
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(powQuery)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                Map<String, Integer> powAmts = new HashMap<>();

                for (String powerupID : POWS.keySet()) {
                    powAmts.put(powerupID, 0);
                }

                while (results.next()) {
                    powAmts.put(results.getString("powerup_id"), results.getInt("amount"));
                }

                return new PowerupsData(
                    this.boarUser.baseQuery().getBlessings(connection),
                    this.boarUser.powQuery().getActiveMiracles(connection) > 0,
                    powAmts
                );
            }
        }
    }

    public QuestData getQuestsData(Connection connection) throws SQLException {
        QuestData questData = new QuestData();

        String questQuery = """
            SELECT
                one_progress,
                one_claimed,
                two_progress,
                two_claimed,
                three_progress,
                three_claimed,
                four_progress,
                four_claimed,
                five_progress,
                five_claimed,
                six_progress,
                six_claimed,
                seven_progress,
                seven_claimed,
                num_completed,
                num_full_completed,
                full_claimed,
                auto_claim
            FROM user_quests
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(questQuery)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    List<Integer> questProgress = new ArrayList<>();
                    List<Boolean> questClaims = new ArrayList<>();

                    questProgress.add(results.getInt("one_progress"));
                    questProgress.add(results.getInt("two_progress"));
                    questProgress.add(results.getInt("three_progress"));
                    questProgress.add(results.getInt("four_progress"));
                    questProgress.add(results.getInt("five_progress"));
                    questProgress.add(results.getInt("six_progress"));
                    questProgress.add(results.getInt("seven_progress"));

                    questClaims.add(results.getBoolean("one_claimed"));
                    questClaims.add(results.getBoolean("two_claimed"));
                    questClaims.add(results.getBoolean("three_claimed"));
                    questClaims.add(results.getBoolean("four_claimed"));
                    questClaims.add(results.getBoolean("five_claimed"));
                    questClaims.add(results.getBoolean("six_claimed"));
                    questClaims.add(results.getBoolean("seven_claimed"));

                    questData = new QuestData(
                        questProgress,
                        questClaims,
                        results.getInt("num_completed"),
                        results.getInt("num_full_completed"),
                        results.getBoolean("full_claimed"),
                        results.getBoolean("auto_claim")
                    );
                }
            }
        }

        return questData;
    }

    public long getFirstJoinedTimestamp(Connection connection) throws SQLException {
        long firstJoinedTimestamp = 0;
        String query = """
            SELECT first_joined_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    firstJoinedTimestamp = results.getTimestamp("first_joined_timestamp").getTime();
                }
            }
        }

        return firstJoinedTimestamp;
    }

    public List<BadgeData> getCurrentBadges(Connection connection) throws SQLException {
        String query = """
            SELECT badge_id, badge_tier, obtained_timestamp, first_obtained_timestamp
            FROM collected_badges
            WHERE user_id = ? AND `exists` = true AND badge_tier >= 0;
        """;

        List<BadgeData> badgeIDs = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    badgeIDs.add(new BadgeData(
                        results.getString("badge_id"),
                        results.getInt("badge_tier"),
                        results.getTimestamp("obtained_timestamp").getTime(),
                        results.getTimestamp("first_obtained_timestamp").getTime()
                    ));
                }
            }
        }

        return badgeIDs;
    }
}
