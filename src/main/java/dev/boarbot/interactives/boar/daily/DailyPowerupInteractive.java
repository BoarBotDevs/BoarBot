package dev.boarbot.interactives.boar.daily;

import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.commands.boar.DailySubcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.MiracleAmountModalHandler;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
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

    private final DailySubcommand callingObj;

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getDaily();

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
        if (!this.initEvent.getUser().getId().equals(compEvent.getUser().getId())) {
            compEvent.deferEdit().queue();
            return;
        }

        if (this.modalHandler != null) {
            this.modalHandler.stop();
        }

        String compID = compEvent.getComponentId().split(",")[1];

        switch(compID) {
            case "POW_SELECT" -> {
                ModalConfig modalConfig = this.config.getModalConfig().get("miracleAmount");

                Modal modal = new ModalImpl(
                    ModalUtil.makeModalID(modalConfig.getId(), compEvent),
                    modalConfig.getTitle(),
                    ModalUtil.makeModalComponents(modalConfig.getComponents())
                );

                this.modalHandler = new MiracleAmountModalHandler(compEvent, this);

                if (this.isStopped) {
                    return;
                }

                compEvent.replyModal(modal).complete();
                this.interaction.getHook().editOriginalComponents(this.getCurComponents()).complete();
            }

            case "SUBMIT_POW" -> {
                compEvent.deferEdit().queue();

                try {
                    BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
                    boarUser.passSynchronizedAction(this);
                    boarUser.decRefs();
                } catch (SQLException exception) {
                    log.error("Failed to get boar user.", exception);
                }
            }

            case "CANCEL_POW" -> this.stop(StopType.EXPIRED);
        }
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            if (this.miraclesToUse <= boarUser.getPowerupAmount(connection, "miracle")) {
                this.stop(StopType.FINISHED);
                boarUser.activateMiracles(connection, this.miraclesToUse);
                this.callingObj.execute();
                return;
            }

            this.miraclesToUse = 0;

            StringConfig strConfig = this.config.getStringConfig();

            EmbedImageGenerator embedGen = new EmbedImageGenerator(strConfig.getDailyPowFailed() + " " + strConfig.getDailyPow());
            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(embedGen.generate().getFileUpload())
                .setComponents(this.getCurComponents());

            if (this.isStopped) {
                return;
            }

            this.interaction.getHook().editOriginal(editedMsg.build()).complete();
        } catch (SQLException exception) {
            log.error("Failed to add boar to database for user (%s)!".formatted(this.user.getName()), exception);
        } catch (IOException exception) {
            log.error("Failed to generate response image.", exception);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (type == StopType.EXPIRED) {
            this.interaction.getHook().deleteOriginal().queue();
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
            this.interaction.getId(), this.COMPONENTS.get("powSelect")
        );
        List<ItemComponent> submitCancelBtns = InteractiveUtil.makeComponents(
            this.interaction.getId(), this.COMPONENTS.get("powSubmitBtn"), this.COMPONENTS.get("powCancelBtn")
        );

        return new ActionRow[] {
            ActionRow.of(powSelect),
            ActionRow.of(submitCancelBtns)
        };
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().complete();

        StringConfig strConfig = this.config.getStringConfig();
        IndivItemConfig miracleConfig = this.config.getItemConfig().getPowerups().get("miracle");

        EmbedImageGenerator embedGen = new EmbedImageGenerator("");
        MessageEditBuilder editedMsg = new MessageEditBuilder();

        try {
            String amountInput = modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
            int amount = Integer.parseInt(amountInput);

            if (amount <= 0) {
                throw new NumberFormatException("Input must be greater than 0.");
            }

            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);

            try (Connection connection = DataUtil.getConnection()) {
                if (amount <= boarUser.getPowerupAmount(connection, "miracle")) {
                    long blessings = boarUser.getBlessings(connection, amount);
                    this.miraclesToUse = amount;

                    String miracleStr = this.miraclesToUse == 1
                        ? miracleConfig.getName()
                        : miracleConfig.getPluralName();
                    String blessingsStr = strConfig.getBlessingsSymbol() + " " + (
                        blessings == 1 ? strConfig.getBlessingsName() : strConfig.getBlessingsPluralName()
                    );

                    embedGen.setStr(
                        this.config.getStringConfig().getDailyPowAttempt().formatted(
                            this.miraclesToUse, miracleStr, blessings, blessingsStr
                        )
                    );
                } else {
                    embedGen.setStr(strConfig.getDailyPowFailed() + " " + strConfig.getDailyPow());
                }
            }

            boarUser.decRefs();

            editedMsg.setFiles(embedGen.generate().getFileUpload()).setComponents(this.getCurComponents());
        } catch (NumberFormatException exception1) {
            try {
                embedGen.setStr(strConfig.getDailyPowInvalid() + " " + strConfig.getDailyPow());
                editedMsg.setFiles(embedGen.generate().getFileUpload()).setComponents(this.getCurComponents());
            } catch (IOException exception2) {
                log.error("Failed to generate invalid input response.", exception2);
            }
        } catch (SQLException exception) {
            log.error("An error occurred when fetching powerup data.", exception);
        } catch (IOException exception) {
            log.error("An error occurred generating response image.", exception);
        }

        if (this.isStopped) {
            return;
        }

        this.interaction.getHook().editOriginal(editedMsg.build()).complete();
    }
}
