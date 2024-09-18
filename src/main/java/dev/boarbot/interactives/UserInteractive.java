package dev.boarbot.interactives;

import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
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
        super(
            interaction.getId() + "," + interaction.getUser().getId(),
            Objects.requireNonNull(interaction.getGuild()).getId()
        );

        this.interaction = interaction;
        this.interactionID = interaction.getId();
        this.user = interaction.getUser();
        this.hook = ((IDeferrableCallback) interaction).getHook();
        this.isMsg = isMsg;
    }

    protected UserInteractive(Interaction interaction, boolean isMsg, long waitTime, long hardTime) {
        super(
            interaction.getId() + "," + interaction.getUser().getId(),
            Objects.requireNonNull(interaction.getGuild()).getId(),
            waitTime,
            hardTime
        );

        this.interaction = interaction;
        this.interactionID = interaction.getId();
        this.user = interaction.getUser();
        this.hook = ((IDeferrableCallback) interaction).getHook();
        this.isMsg = isMsg;
    }

    @Override
    public Message updateInteractive(MessageEditData editedMsg) {
        if (this.msg == null && this.isMsg) {
            ((ComponentInteraction) this.interaction).getHook().sendMessage(
                MessageCreateData.fromEditData(editedMsg)
            ).queue(msg -> this.msg = msg);
        }

        if (this.msg != null) {
            this.msg.editMessage(editedMsg).queue(msg -> this.updateLastEndTime());
            return null;
        }

        this.hook.editOriginal(editedMsg).queue(msg -> this.updateLastEndTime());
        return null;
    }

    @Override
    public Message updateComponents(ActionRow... rows) {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.editMessageComponents(rows).queue(msg -> this.updateLastEndTime());
            return null;
        }

        this.hook.editOriginalComponents(rows).queue(msg -> this.updateLastEndTime());
        return null;
    }

    @Override
    public void deleteInteractive() {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.delete().queue(msg -> this.updateLastEndTime());
        } else {
            this.hook.deleteOriginal().queue(msg -> this.updateLastEndTime());
        }
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (type.equals(StopType.EXCEPTION)) {
            MessageEditBuilder msg = new MessageEditBuilder()
                .setFiles(EmbedImageGenerator.getErrorEmbed())
                .setComponents();

            if (this.hook != null) {
                this.hook.editOriginal(msg.build()).queue();
            } else {
                this.msg.editMessage(msg.build()).queue();
            }

            return;
        }

        if (this.hook != null) {
            this.hook.editOriginalComponents().queue();
        } else {
            this.msg.editMessageComponents().queue();
        }

        Log.debug(this.user, this.getClass(), "Interactive expired");
    }
}
