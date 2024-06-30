package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.data.DataUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class StopMessageListener extends ListenerAdapter implements Runnable {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

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

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.event.getAuthor());
            boolean notificationsOn;

            try (Connection connection = DataUtil.getConnection()) {
                notificationsOn = boarUser.getNotificationStatus(connection);
            }

            if (!notificationsOn) {
                boarUser.decRefs();
                return;
            }

            String[] words = this.event.getMessage().getContentDisplay().split(" ");

            for (String word : words) {
                if (word.trim().equalsIgnoreCase("stop")) {
                    try (Connection connection = DataUtil.getConnection()) {
                        boarUser.setNotifications(connection, null);
                        boarUser.decRefs();

                        this.event.getMessage().reply(this.config.getStringConfig().getNotificationDisabledStr()).queue();
                    }

                    break;
                }
            }
        } catch (SQLException exception) {
            log.error("An error occurred while trying to disable notifications.", exception);
        }
    }
}
