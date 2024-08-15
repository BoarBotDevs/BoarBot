package dev.boarbot.entities.boaruser;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
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

    private boolean alreadyAdded = false;
    private boolean isFirstDaily = false;

    private volatile int numRefs = 0;

    public BoarUser(User user) throws SQLException {
        this.user = user;
        this.userID = user.getId();
        this.incRefs();
    }

    private void addUser(Connection connection) throws SQLException {
        if (this.alreadyAdded) {
            return;
        }

        String query = """
            INSERT IGNORE INTO users (user_id, username) VALUES (?, ?)
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);
            statement.setString(2, this.user.getName());
            statement.execute();
        }

        this.alreadyAdded = true;
    }

    private synchronized void updateUser(Connection connection) throws SQLException {
        String query = """
            SELECT last_daily_timestamp
            FROM users
            WHERE user_id = ?;
        """;

        boolean resetStreak = false;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    Timestamp lastDailyTimestamp = results.getTimestamp("last_daily_timestamp");

                    resetStreak = lastDailyTimestamp != null && lastDailyTimestamp.before(
                        new Timestamp(TimeUtil.getLastDailyResetMilli() - 1000 * 60 * 60 * 24)
                    );
                }
            }
        }

        if (resetStreak) {
            query = """
                UPDATE users
                SET boar_streak = 0
                WHERE user_id = ?
            """;

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, this.userID);
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
            this.giveFirstBonus(connection);
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

                        if (curEdition == 1 && this.config.getRarityConfigs().get(rarityKey).isGivesSpecial()) {
                            this.addFirstBoar(newBoarIDs, connection, bucksGotten, boarEditions);
                        }
                    }
                }
            }
        }

        boarIDs.clear();
        boarIDs.addAll(newBoarIDs);
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
        String query = """
            SELECT
                collected_boars.boar_id,
                COUNT(*) AS amount,
                rarity_id,
                MIN(obtained_timestamp) AS first_obtained,
                MAX(obtained_timestamp) AS last_obtained
            FROM collected_boars, boars_info
            WHERE
                user_id = ? AND
                collected_boars.boar_id = boars_info.boar_id AND
                collected_boars.`exists` = true AND
                collected_boars.deleted = false
            GROUP BY boar_id;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    boarInfo.put(results.getString("boar_id"), new BoarInfo(
                        results.getInt("amount"),
                        results.getString("rarity_id"),
                        results.getTimestamp("first_obtained").getTime(),
                        results.getTimestamp("last_obtained").getTime()
                    ));
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
                blessings,
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
                        results.getInt("blessings"),
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

    private void giveFirstBonus(Connection connection) throws SQLException {
        this.insertPowerupIfNotExist(connection, "miracle");
        this.insertPowerupIfNotExist(connection, "gift");

        String updateQuery = """
            UPDATE collected_powerups
            SET amount = CASE
                WHEN powerup_id = ? THEN amount + 5
                WHEN powerup_id = ? THEN amount + 1
            END
            WHERE user_id = ? AND powerup_id IN (?, ?);
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, "miracle");
            statement.setString(2, "gift");
            statement.setString(3, this.userID);
            statement.setString(4, "miracle");
            statement.setString(5, "gift");
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
        String query = """
            UPDATE users
            SET miracles_active = miracles_active + ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setString(2, this.userID);
            statement.executeUpdate();
        }

        this.usePowerup(connection, "miracle", amount);
    }

    public void useActiveMiracles(Connection connection) throws SQLException {
        String query = """
            UPDATE users
            SET miracles_active = 0
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);
            statement.executeUpdate();
        }
    }

    public List<String> getCurrentBadges(Connection connection) throws SQLException {
        String query = """
            SELECT badge_id
            FROM collected_badges
            WHERE user_id = ? AND has_badge = true;
        """;

        List<String> badgeIDs = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    badgeIDs.add(results.getString("badge_id"));
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
