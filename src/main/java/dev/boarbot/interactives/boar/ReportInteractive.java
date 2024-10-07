package dev.boarbot.interactives.boar;

import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.io.IOException;

public class ReportInteractive extends ModalInteractive {
    private final SlashCommandInteraction interaction;
    private final ForumChannel reportChannel;

    public ReportInteractive(SlashCommandInteractionEvent event) {
        super(event, false, NUMS.getReportIdle(), NUMS.getReportIdle());

        this.interaction = event.getInteraction();
        this.reportChannel = event.getJDA().getForumChannelById(CONFIG.getMainConfig().getReportsChannel());
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        Modal modal = ModalUtil.getModal(CONFIG.getModalConfig().get("reportInput"), compEvent);
        this.modalHandler = new ModalHandler(this.interaction, this);
        this.interaction.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
        Log.debug(this.user, this.getClass(), "Sent report modal");
    }

    @Override
    public void stop(StopType stopType) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        Log.debug(this.user, this.getClass(), "Interactive expired");
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(modalEvent, this, e));

        if (this.reportChannel == null) {
            this.stop(StopType.FINISHED);
            return;
        }

        String title = modalEvent.getValues().getFirst().getAsString();
        String text = "## Report by <@%s> - %s\n".formatted(this.user.getId(), this.user.getName()) +
            modalEvent.getValues().get(1).getAsString();

        if (!modalEvent.getValues().get(2).getAsString().isEmpty()) {
            text += "\n-# Media: %s".formatted(modalEvent.getValues().get(2).getAsString());
        }

        MessageCreateBuilder forumPost = new MessageCreateBuilder().setContent(text);
        MessageCreateBuilder responseMsg = new MessageCreateBuilder();

        try {
            responseMsg.setFiles(
                new EmbedImageGenerator(STRS.getReportSuccess(), COLORS.get("green")).generate().getFileUpload()
            );
        } catch (IOException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate report response", exception);
        }

        this.reportChannel.createForumPost(title, forumPost.build()).queue(
            ch -> modalEvent.getHook().sendMessage(responseMsg.build()).setEphemeral(true)
                .queue(null, e -> ExceptionHandler.replyHandle(this.interaction.getHook(), this, e)),
            e -> {
                Log.error(this.user, this.getClass(), "Failed to send report", e);
                ExceptionHandler.handle(this.user, this.getClass(), e);
            }
        );

        this.stop(StopType.FINISHED);
    }

    @Override
    public ActionRow[] getCurComponents() {
        return new ActionRow[0];
    }
}
