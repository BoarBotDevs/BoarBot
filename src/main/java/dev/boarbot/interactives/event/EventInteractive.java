package dev.boarbot.interactives.event;

import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.Semaphore;

public abstract class EventInteractive extends Interactive {
    protected final TextChannel channel;
    @Getter protected Message msg;

    public static final Semaphore semaphore = new Semaphore(1);

    protected EventInteractive(TextChannel channel) {
        super(channel.getId() + TimeUtil.getCurMilli(), channel.getGuild().getId());
        this.channel = channel;
    }

    protected EventInteractive(TextChannel channel, long waitTime, long hardTime) {
        super(channel.getId() + TimeUtil.getCurMilli(), channel.getGuild().getId(), waitTime, hardTime);
        this.channel = channel;
    }

    @Override
    public void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        this.execute(compEvent);
    }

    @Override
    public abstract void updateInteractive(boolean stopping, MessageEditData editedMsg);

    @Override
    public void updateComponents(boolean stopping, ActionRow... rows) {
        if (this.isStopped && !stopping) {
            return;
        }

        if (this.msg == null) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        semaphore.acquireUninterruptibly();

        this.msg.editMessageComponents(rows).queue(
            m -> semaphore.release(),
            e -> {
                semaphore.release();
                ExceptionHandler.messageHandle(this.msg, this, e);
            }
        );
    }

    @Override
    public void deleteInteractive(boolean stopping) {
        if (this.isStopped && !stopping) {
            return;
        }

        if (this.msg == null) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        semaphore.acquireUninterruptibly();

        this.msg.delete().queue(
            m -> semaphore.release(),
            e -> {
                semaphore.release();
                ExceptionHandler.messageHandle(this.msg, this, e);
            }
        );
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (type.equals(StopType.EXCEPTION)) {
            semaphore.acquireUninterruptibly();

            this.msg.editMessage(MessageEditData.fromCreateData(SpecialReply.getErrorMsgData())).queue(
                m -> semaphore.release(),
                e -> {
                    semaphore.release();
                    ExceptionHandler.messageHandle(this.msg, this, e);
                }
            );

            return;
        }

        semaphore.acquireUninterruptibly();

        Log.debug(this.getClass(), "Interactive expired");
        this.msg.editMessageComponents().queue(
            m -> semaphore.release(),
            e -> {
                semaphore.release();
                ExceptionHandler.messageHandle(this.msg, this, e);
            }
        );
    }

    @Override
    public Interactive removeInteractive() {
        return super.removeInteractive();
    }
}
