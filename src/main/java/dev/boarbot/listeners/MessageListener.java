package dev.boarbot.listeners;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.ConfigUpdater;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class MessageListener extends ListenerAdapter implements Runnable, Configured {
    private MessageReceivedEvent event = null;

    public MessageListener() {
        super();
    }

    public MessageListener(MessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(new MessageListener(event)).start();
    }

    @Override
    public void run() {
        boolean fromDM = this.event.getMessage().isFromType(ChannelType.PRIVATE);
        boolean fromBot = this.event.getMessage().getAuthor().isBot();
        boolean isValidDM = fromDM && !fromBot;

        boolean isReleaseMessage = this.event.getChannel().getId().equals(CONFIG.getMainConfig().getReleaseChannel());

        if (!isValidDM && !isReleaseMessage) {
            return;
        }

        if (isReleaseMessage) {
            Log.info(this.getClass(), "Attempting to deploy release");
            handleNewRelease();
            return;
        }

        handleNotificationToggle();
    }

    private void handleNewRelease() {
        File restartFile = Paths.get("trigger/delete_to_deploy").toFile();

        if (!restartFile.exists()) {
            Log.warn(this.getClass(), "Trigger file does not exist");
            return;
        }

        try {
            ConfigUpdater.setMaintenance(true);
            Thread.sleep(5000);
            Files.delete(restartFile.toPath());
        } catch (IOException exception) {
            Log.error(this.getClass(), "Failed to deploy release", exception);

            try {
                ConfigUpdater.setMaintenance(false);
            } catch (IOException exception1) {
                Log.error(this.getClass(), "Failed to update maintenance in file", exception);
                CONFIG.getMainConfig().setMaintenanceMode(false);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            handleNewRelease();
        }
    }

    private void handleNotificationToggle() {
        BoarUser boarUser;
        boolean notificationsOn;

        try (Connection connection = DataUtil.getConnection()) {
            boarUser = BoarUserFactory.getBoarUser(this.event.getAuthor());
            notificationsOn = boarUser.baseQuery().getNotificationStatus(connection);
        } catch (SQLException exception) {
            Log.error(this.event.getAuthor(), this.getClass(), "Failed to get notification status", exception);
            return;
        }

        if (!notificationsOn) {
            return;
        }

        String[] words = this.event.getMessage().getContentDisplay().split(" ");

        for (String word : words) {
            if (!word.trim().equalsIgnoreCase("stop")) {
                continue;
            }

            try (Connection connection = DataUtil.getConnection()) {
                boarUser.baseQuery().setNotifications(connection, null);
                Log.debug(this.event.getAuthor(), this.getClass(), "Disabled notifications");
            } catch (SQLException exception) {
                Log.error(
                    this.event.getAuthor(), this.getClass(), "Failed to turn off notifications", exception
                );
                return;
            }

            this.event.getMessage().reply(STRS.getNotificationDisabledStr())
                .queue(null, e -> ExceptionHandler.handle(this.event.getAuthor(), this.getClass(), e));
            break;
        }
    }
}
