package dev.boarbot.interactives.boar.daily;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.commands.boar.DailySubcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.modal.ModalUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
public class DailyPowerupInteractive extends ModalInteractive implements Synchronizable {
    private ActionRow[] curComponents = new ActionRow[0];

    private ModalHandler modalHandler = null;
    private int miraclesToUse = 0;
    private boolean firstMsg = true;

    private FileUpload currentImageUpload;

    private final DailySubcommand callingObj;

    private final Map<String, IndivComponentConfig> COMPONENTS = CONFIG.getComponentConfig().getDaily();

    public DailyPowerupInteractive(SlashCommandInteractionEvent initEvent, DailySubcommand callingObj) {
        super(initEvent);
        this.callingObj = callingObj;
    }

    @Override
    public void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        this.attemptExecute(compEvent, null, startTime);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent != null) {
            if (!this.user.getId().equals(compEvent.getUser().getId())) {
                compEvent.deferEdit().queue();
                return;
            }

            if (this.modalHandler != null) {
                this.modalHandler.stop();
            }

            String compID = compEvent.getComponentId().split(",")[1];

            switch(compID) {
                case "POW_SELECT" -> {
                    ModalConfig modalConfig = CONFIG.getModalConfig().get("miracleAmount");

                    Modal modal = new ModalImpl(
                        ModalUtil.makeModalID(modalConfig.getId(), compEvent),
                        modalConfig.getTitle(),
                        ModalUtil.makeModalComponents(modalConfig.getComponents())
                    );

                    this.modalHandler = new ModalHandler(compEvent, this);
                    compEvent.replyModal(modal).complete();
                }

                case "SUBMIT_POW" -> {
                    compEvent.deferEdit().queue();

                    BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
                    boarUser.passSynchronizedAction(this);
                    boarUser.decRefs();
                }

                case "CANCEL_POW" -> this.stop(StopType.EXPIRED);
            }
        }

        this.sendResponse();
    }

    private void sendResponse() {
        try {
            if (this.firstMsg) {
                this.currentImageUpload = new EmbedImageGenerator(STRS.getDailyPow()).generate().getFileUpload();
            }

            this.firstMsg = false;

            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(this.currentImageUpload)
                .setComponents(this.getCurComponents());

            if (this.isStopped) {
                return;
            }

            this.updateInteractive(editedMsg.build());
        } catch (IOException exception) {
            log.error("Failed to generate powerup use image.", exception);
        }
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            if (this.miraclesToUse <= boarUser.powQuery().getPowerupAmount(connection, "miracle")) {
                this.stop(StopType.FINISHED);
                boarUser.powQuery().activateMiracles(connection, this.miraclesToUse);
                this.callingObj.execute();
                return;
            }

            this.miraclesToUse = 0;
            this.currentImageUpload = new EmbedImageGenerator(
                STRS.getNoPow().formatted(POWS.get("miracle").getPluralName()) + " " + STRS.getDailyPow()
            ).generate().getFileUpload();
        } catch (SQLException exception) {
            log.error("Failed to add boar to database for user (%s)!".formatted(this.user.getName()), exception);
        } catch (IOException e) {
            log.error("Failed to create embed image");
        }

        this.sendResponse();
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (type == StopType.EXPIRED) {
            this.deleteInteractive();
        }
    }

    @Override
    public ActionRow[] getCurComponents() {
        if (this.curComponents.length == 0) {
            this.curComponents = this.getComponents();
        }

        Button powSubmitBtn = ((Button) this.curComponents[1].getComponents().getFirst()).withDisabled(true);

        if (this.miraclesToUse > 0) {
            powSubmitBtn = powSubmitBtn.withDisabled(false);
        }

        this.curComponents[1].getComponents().set(0, powSubmitBtn);

        return this.curComponents;
    }

    private ActionRow[] getComponents() {
        List<ItemComponent> powSelect = InteractiveUtil.makeComponents(
            this.interactionID, this.COMPONENTS.get("powSelect")
        );
        List<ItemComponent> submitCancelBtns = InteractiveUtil.makeComponents(
            this.interactionID, this.COMPONENTS.get("powSubmitBtn"), this.COMPONENTS.get("powCancelBtn")
        );

        return new ActionRow[] {
            ActionRow.of(powSelect),
            ActionRow.of(submitCancelBtns)
        };
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().complete();

        PowerupItemConfig miracleConfig = POWS.get("miracle");

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);

            try (Connection connection = DataUtil.getConnection()) {
                int numMiraclesHas = boarUser.powQuery().getPowerupAmount(connection, "miracle");

                String amountInput = modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                int amount = Math.min(Integer.parseInt(amountInput), numMiraclesHas);

                if (amount == 0) {
                    throw new NumberFormatException();
                }

                if (amount > 0) {
                    long blessings = boarUser.baseQuery().getBlessings(connection, amount);
                    this.miraclesToUse = amount;

                    this.currentImageUpload = new EmbedImageGenerator(
                        STRS.getMiracleAttempt().formatted(
                            this.miraclesToUse,
                            this.miraclesToUse == 1
                                ? miracleConfig.getName()
                                : miracleConfig.getPluralName(),
                            STRS.getBlessingsPluralName(),
                            TextUtil.getBlessHex(blessings),
                            blessings > 1000
                                ? STRS.getBlessingsSymbol() + " "
                                : "",
                            blessings
                        )
                    ).generate().getFileUpload();
                } else {
                    this.currentImageUpload = new EmbedImageGenerator(
                        STRS.getNoPow().formatted(miracleConfig.getPluralName()) + " " + STRS.getDailyPow()
                    ).generate().getFileUpload();
                }
            }

            boarUser.decRefs();
        } catch (NumberFormatException exception1) {
            try {
                this.currentImageUpload = new EmbedImageGenerator(STRS.getInvalidInput() + " " + STRS.getDailyPow())
                    .generate().getFileUpload();
            } catch (IOException exception2) {
                log.error("Failed to generate invalid input response.", exception2);
            }
        } catch (SQLException exception) {
            log.error("An error occurred when fetching powerup data.", exception);
        } catch (IOException exception) {
            log.error("An error occurred generating response image.", exception);
        }

        this.sendResponse();
    }
}
