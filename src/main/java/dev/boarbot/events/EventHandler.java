package dev.boarbot.events;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public abstract class EventHandler {
    @Getter protected Set<String> failedGuilds = new HashSet<>();

    protected ImageGenerator imageGenerator;
    protected FileUpload currentImage;

    private int numPotential = 0;
    private int numActive = 0;

    public void sendEvent() {
        try {
            this.currentImage = this.imageGenerator.generate().getFileUpload();
        } catch (IOException | URISyntaxException exception) {
            Log.error(this.getClass(), "Failed to generate event image", exception);
            return;
        }

        Map<String, List<TextChannel>> channels;

        try (Connection connection = DataUtil.getConnection()) {
            channels = GuildDataUtil.getAllChannels(connection);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to get channels for event", exception);
            return;
        }

        Log.debug(this.getClass(), "Gathered all guild channels");

        for (String guildID : channels.keySet()) {
            for (TextChannel channel : channels.get(guildID)) {
                try {
                    incNumPotential();
                    this.sendInteractive(channel);
                } catch (RuntimeException exception) {
                    this.decNumPotential();
                    Log.error(this.getClass(), "A problem occurred when sending event", exception);
                }
            }
        }

        Log.debug(this.getClass(), "Sent to all channels");
    }

    protected abstract void sendInteractive(TextChannel channel) throws InsufficientPermissionException;

    protected abstract void handleResults();
    protected void handleAfterSend() {}

    private synchronized void incNumPotential() {
        this.numPotential++;
    }

    public synchronized void decNumPotential() {
        if (--this.numPotential == 0) {
            this.handleAfterSend();
        }
    }

    public synchronized void incNumActive() {
        this.numActive++;
    }

    public synchronized void decNumActive() {
        if (--this.numActive == 0) {
            this.handleResults();
        }
    }
}
