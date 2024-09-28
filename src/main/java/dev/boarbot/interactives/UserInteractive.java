package dev.boarbot.interactives;

import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Objects;

public abstract class UserInteractive extends Interactive {
    private final Interaction interaction;
    @Getter protected final String interactionID;
    @Getter protected final User user;
    protected final InteractionHook hook;
    private Message msg;
    private final boolean isMsg;

    protected UserInteractive(Interaction interaction) {
        this(interaction, false);
    }

    protected UserInteractive(Interaction interaction, boolean isMsg) {
        this(interaction, isMsg, NUMS.getInteractiveIdle(), NUMS.getInteractiveHardStop());
    }

    protected UserInteractive(Interaction interaction, boolean isMsg, long waitTime, long hardTime) {
        this(interaction, isMsg, waitTime, hardTime, true);
    }

    protected UserInteractive(Interaction interaction, boolean isMsg, long waitTime, long hardTime, boolean checkDupe) {
        super(
            interaction.getId() + "," + interaction.getUser().getId(),
            Objects.requireNonNull(interaction.getGuild()).getId(),
            waitTime,
            hardTime,
            checkDupe
        );

        this.interaction = interaction;
        this.interactionID = interaction.getId();
        this.user = interaction.getUser();
        this.hook = ((IReplyCallback) interaction).getHook();
        this.isMsg = isMsg;
    }

    @Override
    public void updateInteractive(boolean stopping, MessageEditData editedMsg) {
        if (this.isStopped && !stopping) {
            return;
        }

        if (this.msg == null && this.isMsg) {
            IReplyCallback compInter = (IReplyCallback) this.interaction;
            compInter.getHook().sendMessage(MessageCreateData.fromEditData(editedMsg)).queue(
                msg -> {
                    this.msg = msg;
                    this.lastEndTime = TimeUtil.getCurMilli() + 300;
                },
                e -> ExceptionHandler.replyHandle(compInter, this, e)
            );
            return;
        }

        if (this.msg != null) {
            this.msg.editMessage(editedMsg).queue(
                msg -> this.lastEndTime = TimeUtil.getCurMilli() + 300,
                e -> ExceptionHandler.messageHandle(this.msg, this, e)
            );
            return;
        }

        this.hook.editOriginal(editedMsg).queue(
            msg -> this.lastEndTime = TimeUtil.getCurMilli() + 300,
            e -> ExceptionHandler.replyHandle((SlashCommandInteraction) this.interaction, this, e));
    }

    @Override
    public void updateComponents(boolean stopping, ActionRow... rows) {
        if (this.isStopped && !stopping) {
            return;
        }

        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.editMessageComponents(rows).queue(
                msg -> this.lastEndTime = TimeUtil.getCurMilli() + 300,
                e -> ExceptionHandler.messageHandle(this.msg, this, e)
            );
            return;
        }

        this.hook.editOriginalComponents(rows).queue(
            msg -> this.lastEndTime = TimeUtil.getCurMilli() + 300,
            e -> ExceptionHandler.replyHandle((SlashCommandInteraction) this.interaction, this, e)
        );
    }

    @Override
    public void deleteInteractive(boolean stopping) {
        if (this.isStopped && !stopping) {
            return;
        }

        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.delete().queue(
                msg -> this.lastEndTime = TimeUtil.getCurMilli() + 300,
                e -> ExceptionHandler.messageHandle(this.msg, this, e)
            );
            return;
        }

        this.hook.deleteOriginal().queue(
            msg -> this.lastEndTime = TimeUtil.getCurMilli() + 300,
            e -> ExceptionHandler.replyHandle((SlashCommandInteraction) this.interaction, this, e)
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
            MessageEditData msgData = MessageEditData.fromCreateData(SpecialReply.getErrorMsgData());

            if (this.msg != null) {
                this.msg.editMessage(msgData).queue(null, e -> ExceptionHandler.messageHandle(this.msg, this, e));
                return;
            }

            this.hook.editOriginal(msgData)
                .queue(null, e -> ExceptionHandler.replyHandle((SlashCommandInteraction) this.interaction, this, e));
            return;
        }

        Log.debug(this.user, this.getClass(), "Interactive expired");

        if (this.msg != null) {
            this.msg.editMessageComponents().queue(null, e -> ExceptionHandler.messageHandle(this.msg, this, e));
            return;
        }

        this.hook.editOriginalComponents()
            .queue(null, e -> ExceptionHandler.replyHandle((SlashCommandInteraction) this.interaction, this, e));
    }
}
