package dev.boarbot.jobs;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.data.BoarDataUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.data.UserDataUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.quartz.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class NotificationEventJob implements Job, Configured {
    @Getter private final static JobDetail job = JobBuilder.newJob(NotificationEventJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 ? * *").inTimeZone(TimeZone.getTimeZone("UTC")))
        .build();
    private final static Map<String, PrivateChannel> userChannels = new HashMap<>();
    private final static Map<Integer, Integer> dynamicValues = new HashMap<>();
    private final static int streakIndex = 10;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<String> notifUserIDs = new ArrayList<>();

        try (Connection connection = DataUtil.getConnection()) {
            updateDynamicValues(connection);
            notifUserIDs = UserDataUtil.getNotifUserIDs(connection);
        } catch (SQLException exception) {
            Log.error(NotificationEventJob.class, "Failed to get relevant notification data", exception);
        }

        for (String notifUserID : notifUserIDs) {
            BoarUser boarUser = BoarUserFactory.getBoarUser(notifUserID);

            try (Connection connection = DataUtil.getConnection()) {
                String notificationStr = getNotificationStr(
                    connection, boarUser.baseQuery().getNotificationChannel(connection), boarUser
                );

                if (!userChannels.containsKey(boarUser.getUserID())) {
                    boarUser.getUser().openPrivateChannel().queue(
                        ch -> userChannels.put(boarUser.getUserID(), ch),
                        e -> Log.warn(boarUser.getUser(), this.getClass(), "Discord exception thrown", e)
                    );
                }

                userChannels.get(boarUser.getUserID()).sendMessage(notificationStr).setSuppressEmbeds(true).queue(
                    null, e -> Log.warn(boarUser.getUser(), this.getClass(), "Discord exception thrown", e)
                );
            } catch (SQLException exception) {
                Log.error(
                    boarUser.getUser(), NotificationEventJob.class, "Failed to get notification channel", exception
                );
            } catch (ErrorResponseException ignored) {}
        }
    }

    private static void updateDynamicValues(Connection connection) throws SQLException {
        dynamicValues.put(3, BoarDataUtil.getTotalUniques(connection)); // Unique boars
        dynamicValues.put(5, UserDataUtil.getTotalUsers(connection)); // Users
        dynamicValues.put(9, GuildDataUtil.getTotalGuilds(connection)); // Servers
        dynamicValues.put(15, BoarDataUtil.getTotalBoars(connection)); // Global boars
    }

    private static String getNotificationStr(
        Connection connection, String channelID, BoarUser boarUser
    ) throws SQLException {
        String notificationEnding = STRS.getNotificationEnding().formatted(channelID);

        if (TimeUtil.isHalloween()) {
            return "## " + STRS.getNotificationHalloween() + notificationEnding;
        }

        if (TimeUtil.isChristmas()) {
            return "## " + STRS.getNotificationChristmas() + notificationEnding;
        }

        int randIndex = (int) (Math.random() * STRS.getNotificationExtras().length);

        for (int key : dynamicValues.keySet()) {
            if (key == randIndex) {
                return "## " + STRS.getNotificationExtras()[randIndex]
                    .formatted(dynamicValues.get(key)) + notificationEnding;
            }
        }

        if (randIndex == streakIndex) {
            int userStreak = boarUser.baseQuery().getStreak(connection);
            return "## " + STRS.getNotificationExtras()[randIndex].formatted(userStreak) + notificationEnding;
        }

        return "## " + STRS.getNotificationExtras()[randIndex] + notificationEnding;
    }

    public static void cacheNotifUsers() throws SQLException {
        JDA jda = BoarBotApp.getBot().getJDA();
        List<String> notifUserIDs;

        try (Connection connection = DataUtil.getConnection()) {
            notifUserIDs = UserDataUtil.getNotifUserIDs(connection);
        }

        for (String notifUserID : notifUserIDs) {
            try {
                jda.retrieveUserById(notifUserID).queue(
                    user -> user.openPrivateChannel().queue(
                        ch -> userChannels.put(notifUserID, ch),
                        e -> Log.warn(user, NotificationEventJob.class, "Discord exception thrown", e)
                    ),
                    e -> Log.warn(NotificationEventJob.class, "Discord exception thrown", e)
                );
            } catch (ErrorResponseException exception) {
                exception.getErrorResponse();
            }
        }

        Log.debug(NotificationEventJob.class, "%,d user(s) cached for notifications".formatted(notifUserIDs.size()));
    }
}
