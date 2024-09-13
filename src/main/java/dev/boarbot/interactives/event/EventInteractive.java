package dev.boarbot.interactives.event;

import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.IOException;

public abstract class EventInteractive extends Interactive {
    private final TextChannel channel;
    private Message msg;

    protected EventInteractive(TextChannel channel) {
        super(channel.getId() + TimeUtil.getCurMilli(), channel.getGuild().getId());
        this.channel = channel;
    }

    protected EventInteractive(TextChannel channel, long waitTime, long hardTime) {
        super(channel.getId() + TimeUtil.getCurMilli(), channel.getGuild().getId(), waitTime, hardTime);
        this.channel = channel;
    }

    @Override
    public synchronized void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        this.execute(compEvent);
    }

    @Override
    public Message updateInteractive(MessageEditData editedMsg) {
        if (this.msg == null) {
            this.msg = this.channel.sendMessage(MessageCreateData.fromEditData(editedMsg)).complete();
            return this.msg;
        }

        return this.msg.editMessage(editedMsg).complete();
    }

    @Override
    public Message updateComponents(ActionRow... rows) {
        if (this.msg == null) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        return this.msg.editMessageComponents(rows).complete();
    }

    @Override
    public void deleteInteractive() {
        if (this.msg == null) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        this.msg.delete().complete();
    }

    @Override
    public void stop(StopType type) throws IOException, InterruptedException {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        this.msg.editMessageComponents().complete();
    }
}
