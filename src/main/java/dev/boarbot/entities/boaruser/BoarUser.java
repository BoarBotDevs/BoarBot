package dev.boarbot.entities.boaruser;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.entities.boaruser.data.*;
import dev.boarbot.interactives.boar.megamenu.SortType;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BoarUser {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    @Getter private final User user;
    @Getter private final String userID;

    private boolean isFirstDaily = false;

    private volatile int numRefs = 0;

    public BoarUser(User user) throws SQLException {
        this.user = user;
        this.userID = user.getId();
        this.incRefs();
    }

    private void addUser(Connection connection) throws SQLException {
        if (this.userExists(connection)) {
            return;
        }

        String query = """
            INSERT INTO users (user_id, username) VALUES (?, ?)
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);
            statement.setString(2, this.user.getName());
            statement.execute();
        }
    }

    public boolean userExists(Connection connection) throws SQLException {
        String query = """
            SELECT user_id
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private synchronized void updateUser(Connection connection) throws SQLException {
        String query = """
            SELECT last_daily_timestamp, last_streak_fix, first_joined_timestamp, boar_streak
            FROM users
            WHERE user_id = ?;
        """;

        long lastDailyLong = 0;
        long lastStreakLong = 0;
        long firstJoinedLong = 0;
        int boarStreak = 0;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
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
        }

        int newBoarStreak = boarStreak;
        long timeToReach = Math.max(Math.max(lastDailyLong, lastStreakLong), firstJoinedLong);
        long curTimeCheck = TimeUtil.getLastDailyResetMilli() - TimeUtil.getOneDayMilli();
        int curRemove = 7;
        int curDailiesMissed = 0;

        while (timeToReach < curTimeCheck) {
            newBoarStreak = Math.max(newBoarStreak - curRemove, 0);
            curTimeCheck -= TimeUtil.getOneDayMilli();
            curRemove *= 2;
            curDailiesMissed++;
        }

        if (curDailiesMissed > 0) {
            query = """
                UPDATE users
                SET boar_streak = ?, num_dailies_missed = num_dailies_missed + ?, last_streak_fix = ?
                WHERE user_id = ?
            """;

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, newBoarStreak);
                statement.setInt(2, curDailiesMissed);
                statement.setTimestamp(3, new Timestamp(TimeUtil.getLastDailyResetMilli()-1));
                statement.setString(4, this.userID);
                statement.executeUpdate();
            }
        }
    }

    public long getLastChanged(Connection connection) throws SQLException {
        long lastChangedTimestamp = TimeUtil.getCurMilli();
        String query = """
            SELECT last_changed_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    lastChangedTimestamp = results.getTimestamp("last_changed_timestamp").getTime();
                }
            }
        }

        return lastChangedTimestamp;
    }

    public synchronized void passSynchronizedAction(Synchronizable callingObject) {
        callingObject.doSynchronizedAction(this);
    }

    public void addBoars(
        List<String> boarIDs,
        Connection connection,
        BoarObtainType obtainType,
        List<Integer> bucksGotten,
        List<Integer> boarEditions
    ) throws SQLException {
        this.addUser(connection);

        List<String> newBoarIDs = new ArrayList<>();

        if (this.isFirstDaily) {
            this.addPowerup(connection, "miracle", 5);
            this.addPowerup(connection, "gift", 1);
        }
        this.isFirstDaily = false;

        for (String boarID : boarIDs) {
            String boarAddQuery = """
                INSERT INTO collected_boars (user_id, boar_id, original_obtain_type)
                VALUES (?, ?, ?)
                RETURNING edition, bucks_gotten;
            """;
            int curEdition;

            try (PreparedStatement boarAddStatement = connection.prepareStatement(boarAddQuery)) {
                boarAddStatement.setString(1, this.userID);
                boarAddStatement.setString(2, boarID);
                boarAddStatement.setString(3, obtainType.toString());

                try (ResultSet results = boarAddStatement.executeQuery()) {
                    if (results.next()) {
                        curEdition = results.getInt("edition");

                        newBoarIDs.add(boarID);
                        boarEditions.add(curEdition);
                        bucksGotten.add(results.getInt("bucks_gotten"));

                        String rarityKey = BoarUtil.findRarityKey(boarID);

                        if (curEdition == 1 && this.config.getRarityConfigs().get(rarityKey).isGivesFirstBoar()) {
                            this.addFirstBoar(newBoarIDs, connection, bucksGotten, boarEditions);
                        }
                    }
                }
            }
        }

        boarIDs.clear();
        boarIDs.addAll(newBoarIDs);
    }

    public void giveBucks(Connection connection, long amount) throws SQLException {
        String query = """
            UPDATE users
            SET total_bucks = total_bucks + ?
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, amount);
            statement.setString(2, this.userID);
            statement.executeUpdate();
        }
    }

    public boolean hasBoar(String boarID, Connection connection) throws SQLException {
        String query = """
            SELECT boar_id
            FROM collected_boars
            WHERE boar_id = ? AND user_id = ? AND `exists` = 1 AND deleted = 0;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, boarID);
            statement.setString(2, this.userID);
            return statement.executeQuery().next();
        }
    }

    public void removeBoar(String boarID, Connection connection) throws SQLException {
        String query = """
            UPDATE collected_boars
            SET deleted = 1
            WHERE boar_id = ? AND user_id = ? AND `exists` = 1 AND deleted = 0
            ORDER BY edition DESC
            LIMIT 1;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, boarID);
            statement.setString(2, this.userID);
            statement.executeUpdate();
        }
    }

    private void addFirstBoar(
        List<String> newBoarIDs,
        Connection connection,
        List<Integer> bucksGotten,
        List<Integer> boarEditions
    ) throws SQLException {
        String insertFirstQuery = """
            INSERT INTO collected_boars (user_id, boar_id, original_obtain_type)
            VALUES (?, ?, ?)
            RETURNING edition;
        """;
        String firstBoarID = this.config.getMainConfig().getFirstBoarID();

        if (!this.config.getItemConfig().getBoars().containsKey(firstBoarID)) {
            return;
        }

        try (PreparedStatement insertFirstStatement = connection.prepareStatement(insertFirstQuery)) {
            insertFirstStatement.setString(1, this.userID);
            insertFirstStatement.setString(2, firstBoarID);
            insertFirstStatement.setString(3, BoarObtainType.OTHER.toString());

            try (ResultSet results = insertFirstStatement.executeQuery()) {
                if (results.next()) {
                    newBoarIDs.add(firstBoarID);
                    boarEditions.add(results.getInt("edition"));
                    bucksGotten.add(0);
                }
            }
        }
    }

    public String getFavoriteID(Connection connection) throws SQLException {
        String favoriteID = null;
        String query = """
            SELECT favorite_boar_id
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

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
            statement.setString(2, this.userID);
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
            statement.setString(1, this.userID);

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
            statement.setString(2, this.userID);
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
            statement.setString(1, this.userID);

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
            statement.setString(2, this.userID);
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
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    boarInfo.put(results.getString("boar_id"), new BoarInfo(
                        results.getString("rarity_id")
                    ));
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(secondQuery)) {
            statement.setString(1, this.userID);

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
                other_bless
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

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
                        this.getBlessings(connection),
                        results.getInt("streak_bless"),
                        results.getInt("unique_bless"),
                        results.getInt("quest_bless"),
                        results.getInt("other_bless")
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
                total_bucks,
                highest_bucks,
                num_dailies,
                num_dailies_missed,
                last_daily_timestamp,
                last_boar_id,
                favorite_boar_id,
                total_boars,
                highest_boars,
                unique_boars,
                highest_unique_boars,
                boar_streak,
                highest_streak,
                notifications_on,
                highest_blessings,
                streak_bless,
                highest_streak_bless,
                quest_bless,
                highest_quest_bless,
                unique_bless,
                highest_unique_bless,
                other_bless,
                highest_other_bless,
                powerup_attempts,
                powerup_wins,
                powerup_perfects,
                powerup_fastest_time,
                (
                    SELECT SUM(average_placement) / GREATEST(COUNT(average_placement), 1)
                    FROM prompt_stats
                    WHERE user_id = ?
                ) AS powerup_average_placement,
                miracles_active,
                miracle_rolls,
                highest_miracles_active,
                miracle_best_bucks,
                miracle_best_rarity,
                (
                    SELECT boar_id
                    FROM collected_boars
                    WHERE
                        collected_boars.user_id = ? AND
                        collected_boars.original_obtain_type = 'TRANSMUTE' AND
                        collected_boars.exists = true AND
                        collected_boars.deleted = false
                    ORDER BY unique_id DESC LIMIT 1
                ) AS last_transmute,
                (
                    SELECT boar_id
                    FROM collected_boars
                    WHERE
                        collected_boars.user_id = ? AND
                        collected_boars.original_obtain_type = 'CLONE' AND
                        collected_boars.exists = true AND
                        collected_boars.deleted = false
                    ORDER BY unique_id DESC LIMIT 1
                ) AS last_clone,
                gift_handicap,
                gifts_opened,
                gift_fastest,
                gift_best_bucks,
                gift_best_rarity
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
            statement1.setString(1, this.userID);
            statement1.setString(2, this.userID);
            statement1.setString(3, this.userID);
            statement1.setString(4, this.userID);

            statement2.setString(1, this.userID);

            statement3.setString(1, this.userID);

            statement4.setString(1, this.userID);

            statement5.setString(1, this.userID);

            statement6.setString(1, this.userID);

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
                        this.getBlessings(connection),
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
        PowerupsData powData;

        String powQuery = """
            SELECT powerup_id, amount
            FROM collected_powerups
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(powQuery)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                Map<String, Integer> powAmts = new HashMap<>();

                while (results.next()) {
                    powAmts.put(results.getString("powerup_id"), results.getInt("amount"));
                }

                powData = new PowerupsData(
                    this.getBlessings(connection),
                    powAmts
                );
            }
        }

        return powData;
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
                full_claimed
            FROM user_quests
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(questQuery)) {
            statement.setString(1, this.userID);

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
                        results.getBoolean("full_claimed")
                    );
                }
            }
        }

        return questData;
    }

    public boolean canUseDaily(Connection connection) throws SQLException {
        this.addUser(connection);

        boolean canUseDaily = false;
        String query = """
            SELECT last_daily_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    Timestamp lastDailyTimestamp = results.getTimestamp("last_daily_timestamp");

                    canUseDaily = lastDailyTimestamp == null ||
                        lastDailyTimestamp.getTime() < TimeUtil.getLastDailyResetMilli();

                    this.isFirstDaily = lastDailyTimestamp == null;
                }
            }
        }

        return canUseDaily;
    }

    public void addPowerup(Connection connection, String powerupID, int amount) throws SQLException {
        this.insertPowerupIfNotExist(connection, powerupID);

        String updateQuery = """
            UPDATE collected_powerups
            SET amount = amount + ?
            WHERE user_id = ? AND powerup_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, amount);
            statement.setString(2, this.userID);
            statement.setString(3, powerupID);
            statement.execute();
        }
    }

    private void insertPowerupIfNotExist(Connection connection, String powerupID) throws SQLException {
        String query = """
            INSERT INTO collected_powerups (user_id, powerup_id)
            SELECT ?, ?
            WHERE NOT EXISTS (
                SELECT unique_id
                FROM collected_powerups
                WHERE user_id = ? AND powerup_id = ?
            );
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);
            statement.setString(2, powerupID);
            statement.setString(3, this.userID);
            statement.setString(4, powerupID);
            statement.execute();
        }
    }

    public boolean isFirstDaily() {
        return this.isFirstDaily;
    }

    public long getFirstJoinedTimestamp(Connection connection) throws SQLException {
        long firstJoinedTimestamp = 0;
        String query = """
            SELECT first_joined_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    firstJoinedTimestamp = results.getTimestamp("first_joined_timestamp").getTime();
                }
            }
        }

        return firstJoinedTimestamp;
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
            blessingsStatement.setString(1, this.userID);

            try (ResultSet results = blessingsStatement.executeQuery()) {
                if (results.next()) {
                    int miraclesActive = results.getInt("miracles_active");
                    blessings = results.getLong("blessings");
                    int miracleIncreaseMax = this.config.getNumberConfig().getMiracleIncreaseMax();

                    int activesLeft = miraclesActive+extraActive;
                    for (; activesLeft>0; activesLeft--) {
                        long amountToAdd = (long) Math.min(Math.ceil(blessings * 0.1), miracleIncreaseMax);

                        if (amountToAdd == this.config.getNumberConfig().getMiracleIncreaseMax()) {
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

    public void setNotifications(Connection connection, String channelID) throws SQLException {
        String query = """
            UPDATE users
            SET notifications_on = ?, notification_channel = ?
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, channelID != null);
            statement.setString(2, channelID);
            statement.setString(3, this.userID);
            statement.executeUpdate();
        }
    }

    public boolean getNotificationStatus(Connection connection) throws SQLException {
        String query = """
            SELECT notifications_on
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getBoolean("notifications_on");
                }
            }
        }

        return false;
    }

    public int getPowerupAmount(Connection connection, String powerupID) throws SQLException {
        String query = """
            SELECT amount
            FROM collected_powerups
            WHERE user_id = ? AND powerup_id = ?;
        """;

        int amount = 0;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);
            statement.setString(2, powerupID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    amount = results.getInt("amount");
                }
            }
        }

        return amount;
    }

    public void usePowerup(Connection connection, String powerupID, int amount) throws SQLException {
        String query = """
            UPDATE collected_powerups
            SET amount = amount - ?, amount_used = amount_used + ?
            WHERE user_id = ? AND powerup_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setInt(2, amount);
            statement.setString(3, this.userID);
            statement.setString(4, powerupID);
            statement.executeUpdate();
        }
    }

    public void activateMiracles(Connection connection, int amount) throws SQLException {
        String updateQuery = """
            UPDATE users
            SET
                miracles_active = miracles_active + ?,
                highest_blessings = GREATEST(highest_blessings, ?),
                miracle_rolls = miracle_rolls + 1
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, amount);
            statement.setLong(2, this.getBlessings(connection, amount));
            statement.setString(3, this.userID);
            statement.executeUpdate();
        }

        this.usePowerup(connection, "miracle", amount);
    }

    public void useActiveMiracles(
        List<String> boarIDs, List<Integer> bucksGotten, Connection connection
    ) throws SQLException {
        int totalBucks = bucksGotten.stream().reduce(0, Integer::sum);
        String bestRarity = null;

        for (String boarID : boarIDs) {
            String boarRarity = BoarUtil.findRarityKey(boarID);

            if (bestRarity == null) {
                bestRarity = boarRarity;
                continue;
            }

            bestRarity = BoarUtil.getHigherRarity(bestRarity, boarRarity);
        }

        String bestRarityQuery = """
            SELECT miracle_best_rarity
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(bestRarityQuery)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next() && results.getString("miracle_best_rarity") != null) {
                    bestRarity = BoarUtil.getHigherRarity(bestRarity, results.getString("miracle_best_rarity"));
                }
            }
        }

        String updateQuery = """
            UPDATE users
            SET miracles_active = 0, miracle_best_bucks = GREATEST(miracle_best_bucks, ?), miracle_best_rarity = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, totalBucks);
            statement.setString(2, bestRarity);
            statement.setString(3, this.userID);
            statement.executeUpdate();
        }
    }

    public int getGiftHandicap(Connection connection) throws SQLException {
        this.addUser(connection);
        int handicap = 0;

        String query = """
            SELECT gift_handicap
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    handicap = results.getInt("gift_handicap");
                }
            }
        }

        return handicap;
    }

    public void updateGiftHandicap(Connection connection, long value) throws SQLException {
        int MAX_HANDICAP_WEIGHT = 20;

        long handicapValue = value * -1;
        if (handicapValue < this.config.getNumberConfig().getGiftMaxHandicap() * -1) {
            String handicapQuery = """
                SELECT gift_handicap
                FROM users
                WHERE user_id = ?;
            """;

            try (PreparedStatement statement = connection.prepareStatement(handicapQuery)) {
                statement.setString(1, this.userID);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        handicapValue = results.getLong("gift_handicap");
                    }
                }
            }
        }

        String updateQuery = """
            UPDATE users
            SET
                gift_handicap = GREATEST(
                    ((gift_handicap * gift_handicap_weight) + ?) / (gift_handicap_weight + 1),
                    ?
                ),
                gift_handicap_weight = LEAST(gift_handicap_weight + 1, ?),
                gift_fastest = LEAST(gift_fastest, ?)
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setLong(1, handicapValue);
            statement.setLong(2, handicapValue);
            statement.setInt(3, MAX_HANDICAP_WEIGHT);
            statement.setLong(4, value);
            statement.setString(5, this.userID);
            statement.executeUpdate();
        }
    }

    public void openGift(Connection connection, int bucks, List<String> rarityKeys, boolean incOpen) throws SQLException {
        String bestRarity = null;

        String bestRarityQuery = """
            SELECT gift_best_rarity
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(bestRarityQuery)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    bestRarity = results.getString("gift_best_rarity");
                    for (String rarityKey : rarityKeys) {
                        bestRarity = BoarUtil.getHigherRarity(bestRarity, rarityKey);
                    }
                }
            }
        }

        String openGiftQuery = """
            UPDATE users
            SET
                gifts_opened = gifts_opened + ?,
                gift_best_bucks = GREATEST(gift_best_bucks, ?),
                gift_best_rarity = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(openGiftQuery)) {
            statement.setInt(1, incOpen ? 1 : 0);
            statement.setInt(2, bucks);
            statement.setString(3, bestRarity);
            statement.setString(4, this.userID);
            statement.executeUpdate();
        }
    }

    public List<BadgeData> getCurrentBadges(Connection connection) throws SQLException {
        String query = """
            SELECT badge_id, badge_tier, obtained_timestamp
            FROM collected_badges
            WHERE user_id = ? AND `exists` = true;
        """;

        List<BadgeData> badgeIDs = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    badgeIDs.add(new BadgeData(
                        results.getString("badge_id"),
                        results.getInt("badge_tier"),
                        results.getTimestamp("obtained_timestamp").getTime()
                    ));
                }
            }
        }

        return badgeIDs;
    }

    public synchronized void incRefs() throws SQLException {
        this.numRefs++;

        try (Connection connection = DataUtil.getConnection()) {
            this.updateUser(connection);
        }
    }

    public synchronized void decRefs() {
        this.numRefs--;

        if (this.numRefs == 0) {
            BoarUserFactory.removeBoarUser(this.userID);
        }
    }
}
