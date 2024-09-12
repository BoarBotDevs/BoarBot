package dev.boarbot.events;

import dev.boarbot.interactives.event.PowerupEventInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class PowerupEventHandler extends EventHandler {
    @Getter private static final List<Message> curMessages = new ArrayList<>();
    private static final Set<Message> priorMessages = new HashSet<>();

    @Getter private final PromptType promptType;
    @Getter private final String promptID;

    public PowerupEventHandler() {
        PromptType[] promptTypes = PromptType.values();
        this.promptType = promptTypes[(int) (Math.random() * promptTypes.length)];

        String[] promptIDs = CONFIG.getPromptConfig().get(this.promptType.toString()).getPrompts().keySet()
            .toArray(new String[0]);
        this.promptID = promptIDs[(int) (Math.random() * promptIDs.length)];
    }

    @Override
    public void sendEvent() {
        this.setPriorMessages();
        super.sendEvent();

        try (Connection connection = DataUtil.getConnection()) {
            GuildDataUtil.updatePowerupMessages(connection, curMessages);
        } catch (SQLException exception) {
            log.error("Failed to update Powerup Event messages in database.", exception);
        }

        this.removePriorEvent();
    }

    @Override
    protected PowerupEventInteractive sendInteractive(TextChannel channel) throws InsufficientPermissionException {
        PowerupEventInteractive interactive = new PowerupEventInteractive(channel, null, this);
        interactive.execute(null);
        return interactive;
    }

    private void setPriorMessages() {
        priorMessages.clear();

        if (!curMessages.isEmpty()) {
            priorMessages.addAll(curMessages);
            curMessages.clear();
            return;
        }

        try (Connection connection = DataUtil.getConnection()) {
            priorMessages.addAll(GuildDataUtil.getPowerupMessages(connection));
        } catch (SQLException exception) {
            log.error("Failed to get prior Powerup Event messages to remove.", exception);
        }
    }

    private void removePriorEvent() {
        for (Message msg : priorMessages) {
            try {
                msg.delete().queue();
            } catch (ErrorResponseException ignored) {}
        }
    }

    @Override
    protected void handleResults() {

    }
}
