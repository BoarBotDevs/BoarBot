package dev.boarbot.entities.boaruser;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.commands.boar.DailySubcommand;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;
import java.util.*;

@Log4j2
public class BoarUser {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    @Getter private final User user;
    @Getter private final String userID;

    private boolean alreadyAdded = false;

    private volatile int numRefs = 1;

    public BoarUser(User user) {
        this.user = user;
        this.userID = user.getId();
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

    public synchronized void doSynchronizedAction(BoarUserAction action, Object callingObject) {
        switch (action) {
            case BoarUserAction.DAILY -> ((DailySubcommand) callingObject).doDaily(this);
        }
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
        String firstBoarID = this.config.getStringConfig().getFirstBoarID();

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
                    canUseDaily = results.getTimestamp("last_daily_timestamp") == null || lastDailyTimestamp.before(
                        new Timestamp(TimeUtil.getLastDailyResetMilli())
                    );
                }
            }
        }

        return canUseDaily;
    }

    public long getMultiplier(Connection connection) throws SQLException {
        long multiplier = 0;

        String multiplierQuery = """
            SELECT multiplier, miracles_active
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement multiplierStatement = connection.prepareStatement(multiplierQuery)) {
            multiplierStatement.setString(1, this.userID);

            try (ResultSet results = multiplierStatement.executeQuery()) {
                if (results.next()) {
                    int miraclesActive = results.getInt("miracles_active");

                    multiplier = results.getLong("multiplier");

                    for (int i=0; i<miraclesActive; i++) {
                        multiplier += (long) Math.min(
                            Math.ceil(multiplier * 0.1), this.config.getNumberConfig().getMiracleIncreaseMax()
                        );
                    }
                }
            }
        }

        return multiplier;
    }

    public void enableNotifications(Connection connection, String channelID) throws SQLException {
        String query = """
            UPDATE users
            SET notifications_on = true, notification_channel = ?
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, channelID);
            statement.setString(2, this.userID);
            statement.executeUpdate();
        }
    }

    public void disableNotifications(Connection connection) throws SQLException {
        String query = """
            UPDATE users
            SET notifications_on = false, notification_channel = null
            WHERE user_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.userID);
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

    public synchronized void incRefs() {
        this.numRefs++;
    }

    public synchronized void decRefs() {
        this.numRefs--;

        if (this.numRefs == 0) {
            BoarUserFactory.removeBoarUser(this.userID);
        }
    }
}
