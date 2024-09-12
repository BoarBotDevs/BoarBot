package dev.boarbot.events;

import dev.boarbot.interactives.event.PowerupEventInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class PowerupEventHandler extends EventHandler {
    private static final List<Message> curMessages = new ArrayList<>();
    private static final Set<Message> priorMessages = new HashSet<>();

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
        PowerupEventInteractive interactive = new PowerupEventInteractive(channel);
        Message interactiveMsg = interactive.updateInteractive(new MessageEditBuilder().setContent("hello!").build());

        curMessages.add(interactiveMsg);

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
            msg.delete().queue();
        }
    }

    @Override
    protected void handleResults() {

    }
}
