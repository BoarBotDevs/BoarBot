package dev.boarbot.interactives.boar;

import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class WipeInteractive extends ModalInteractive implements Synchronizable {
    private final SlashCommandInteraction interaction;

    public WipeInteractive(SlashCommandInteractionEvent event) {
        super(event);
        this.interaction = event.getInteraction();
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        Modal modal = ModalUtil.getModal(CONFIG.getModalConfig().get("wipeInput"), this.interaction);
        this.modalHandler = new ModalHandler(this.interaction, this, NUMS.getInteractiveIdle());
        this.interaction.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
        Log.debug(this.user, this.getClass(), "Sent wipe modal");
    }

    @Override
    public void stop(StopType stopType) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (stopType.equals(StopType.EXCEPTION)) {
            super.stop(stopType);
            return;
        }

        Log.debug(this.user, this.getClass(), "Interactive expired");
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(modalEvent, this, e));

        String firstInput = modalEvent.getValues().getFirst().getAsString();
        String secondInput = modalEvent.getValues().get(1).getAsString();

        MessageCreateBuilder msg = new MessageCreateBuilder();

        try {
            if (!firstInput.equalsIgnoreCase(STRS.getWipeCheckOneStr())) {
                msg.setFiles(new EmbedImageGenerator(STRS.getWipeCheckOneFail()).generate().getFileUpload());
                modalEvent.getHook().sendMessage(msg.build()).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.interaction.getHook(), this, e));

                this.stop(StopType.FINISHED);
                return;
            }

            if (!secondInput.equalsIgnoreCase(modalEvent.getUser().getName())) {
                msg.setFiles(new EmbedImageGenerator(
                    STRS.getWipeCheckTwoFail().formatted(modalEvent.getUser().getName())
                ).generate().getFileUpload());
                modalEvent.getHook().sendMessage(msg.build()).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.interaction.getHook(), this, e));

                this.stop(StopType.FINISHED);
                return;
            }

            BoarUser boarUser = BoarUserFactory.getBoarUser(modalEvent.getUser());
            boarUser.passSynchronizedAction(this);

            msg.setFiles(
                new EmbedImageGenerator(STRS.getWipeSuccess(), COLORS.get("green")).generate().getFileUpload()
            );
            modalEvent.getHook().sendMessage(msg.build()).setEphemeral(true)
                .queue(null, e -> ExceptionHandler.replyHandle(this.interaction.getHook(), this, e));

            this.stop(StopType.FINISHED);
        } catch (IOException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate response message", exception);
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update data", exception);
        }
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.baseQuery().wipeUser(connection);
            Log.info(this.user, this.getClass(), "Wiped their data");
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to remove data", exception);
        }
    }

    @Override
    public ActionRow[] getCurComponents() {
        return new ActionRow[0];
    }
}
