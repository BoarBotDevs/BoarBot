package dev.boarbot.interactives;

import dev.boarbot.util.interactive.StopType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.IOException;
import java.util.Objects;

public abstract class UserInteractive extends Interactive {
    private final Interaction interaction;
    @Getter protected final String interactionID;
    @Getter protected final User user;
    protected final InteractionHook hook;
    protected Message msg;
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

    protected UserInteractive(Interaction interaction, boolean isMsg, long waitTime) {
        super(
            interaction.getId() + "," + interaction.getUser().getId(),
            Objects.requireNonNull(interaction.getGuild()).getId(),
            waitTime
        );

        this.interaction = interaction;
        this.interactionID = interaction.getId();
        this.user = interaction.getUser();
        this.hook = ((IDeferrableCallback) interaction).getHook();
        this.isMsg = isMsg;
    }

    public abstract void execute(GenericComponentInteractionCreateEvent compEvent);
    public abstract ActionRow[] getCurComponents();

    @Override
    public void updateInteractive(MessageEditData editedMsg) {
        if (this.msg == null && this.isMsg) {
            this.msg = ((ComponentInteraction) this.interaction).getHook().sendMessage(
                    MessageCreateData.fromEditData(editedMsg)
            ).complete();
            return;
        }

        if (this.msg != null) {
            this.msg.editMessage(editedMsg).complete();
        } else {
            this.hook.editOriginal(editedMsg).complete();
        }
    }

    @Override
    public void updateComponents(ActionRow... rows) {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.editMessageComponents(rows).complete();
        } else {
            this.hook.editOriginalComponents(rows).complete();
        }
    }

    @Override
    public void deleteInteractiveMessage() {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.delete().complete();
        } else {
            this.hook.deleteOriginal().complete();
        }
    }

    @Override
    public void stop(StopType type) throws IOException, InterruptedException {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (this.hook != null) {
            this.hook.editOriginalComponents().complete();
        } else {
            this.msg.editMessageComponents().complete();
        }
    }
}
