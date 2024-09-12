package dev.boarbot.events;

import dev.boarbot.interactives.event.EventInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public abstract class EventHandler {
    protected Set<String> failedGuilds = new HashSet<>();
    protected Set<EventInteractive> eventInteractives = new HashSet<>();

    public void sendEvent() {
        Map<String, List<TextChannel>> channels = new HashMap<>();

        try (Connection connection = DataUtil.getConnection()) {
            channels = GuildDataUtil.getAllChannels(connection);
        } catch (SQLException exception) {
            log.error("Failed to get channels for powerup event.", exception);
        }

        for (String guildID : channels.keySet()) {
            boolean failed = true;

            for (TextChannel channel : channels.get(guildID)) {
                try {
                    this.eventInteractives.add(this.sendInteractive(channel));
                    failed = false;
                } catch (InsufficientPermissionException ignored) {}
            }

            if (failed) {
                this.failedGuilds.add(guildID);
            }
        }
    }

    protected abstract EventInteractive sendInteractive(TextChannel channel) throws InsufficientPermissionException;

    protected abstract void handleResults();
}
