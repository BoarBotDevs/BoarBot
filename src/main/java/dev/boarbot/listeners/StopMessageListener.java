package dev.boarbot.listeners;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class StopMessageListener extends ListenerAdapter implements Runnable, Configured {
    private MessageReceivedEvent event = null;

    public StopMessageListener() {
        super();
    }

    public StopMessageListener(MessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(new StopMessageListener(event)).start();
    }

    @Override
    public void run() {
        boolean fromDM = this.event.getMessage().isFromType(ChannelType.PRIVATE);
        boolean fromAuthor = this.event.getMessage().getAuthor().isBot();
        boolean ignoreMsg = !fromDM || fromAuthor;

        if (ignoreMsg) {
            return;
        }

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
