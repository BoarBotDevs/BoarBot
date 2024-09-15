package dev.boarbot.events;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.ImageGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.FileUpload;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class EventHandler {
    protected Set<String> failedGuilds = new HashSet<>();

    protected ImageGenerator imageGenerator;
    protected FileUpload currentImage;

    private int numPotential = 0;
    private int numActive = 0;

    public void sendEvent() {
        try {
            this.currentImage = this.imageGenerator.generate().getFileUpload();
        } catch (Exception exception) {
            log.error("Failed to generate event image.", exception);
        }

        Map<String, List<TextChannel>> channels = new HashMap<>();

        try (Connection connection = DataUtil.getConnection()) {
            channels = GuildDataUtil.getAllChannels(connection);
        } catch (SQLException exception) {
            log.error("Failed to get channels for powerup event.", exception);
        }

        for (String guildID : channels.keySet()) {
            for (TextChannel channel : channels.get(guildID)) {
                this.numPotential++;

                CompletableFuture.runAsync(() -> {
                    try {
                        this.sendInteractive(channel);
                        this.incNumActive();
                    } catch (InsufficientPermissionException exception) {
                        this.failedGuilds.add(guildID);
                    }

                    this.decNumPotential();
                });
            }
        }
    }

    protected abstract void sendInteractive(TextChannel channel) throws InsufficientPermissionException;

    protected abstract void handleResults();
    protected void handleAfterSend() {}

    private synchronized void decNumPotential() {
        if (--this.numPotential == 0) {
            this.handleAfterSend();
        }
    }

    private synchronized void incNumActive() {
        this.numActive++;
    }

    public synchronized void decNumActive() {
        if (--this.numActive == 0) {
            this.handleResults();
        }
    }
}
