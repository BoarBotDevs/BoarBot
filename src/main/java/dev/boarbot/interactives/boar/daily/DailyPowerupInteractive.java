package dev.boarbot.interactives.boar.daily;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.commands.boar.DailySubcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DailyPowerupInteractive extends ModalInteractive {
    private ModalHandler modalHandler = null;
    private int miraclesToUse = 0;
    private boolean firstMsg = true;

    private FileUpload currentImageUpload;

    private final DailySubcommand callingObj;

    private static final Map<String, IndivComponentConfig> COMPONENTS = CONFIG.getComponentConfig().getDaily();

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
        if (compEvent == null) {
            this.sendResponse();
            return;
        }

        if (this.modalHandler != null) {
            this.modalHandler.stop();
        }

        String compID = compEvent.getComponentId().split(",")[1];

        switch(compID) {
            case "POW_SELECT" -> {
                Modal modal = ModalUtil.getModal(CONFIG.getModalConfig().get("miracleAmount"), compEvent);
                this.modalHandler = new ModalHandler(compEvent, this, NUMS.getInteractiveIdle());
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent miracle input modal");
            }

            case "SUBMIT_POW" -> {
                compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(compEvent, this, e));

                try {
                    BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
                    boarUser.passSynchronizedAction(() -> this.usePowerups(boarUser));
                } catch (SQLException exception) {
                    this.stop(StopType.EXCEPTION);
                    Log.error(this.user, this.getClass(), "Failed to update data", exception);
                    return;
                }
            }

            case "CANCEL_POW" -> this.stop(StopType.EXPIRED);
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

            this.updateInteractive(false, editedMsg.build());
        } catch (IOException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate powerup use message", exception);
        }
    }

    public void usePowerups(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            int numMiracles = boarUser.powQuery().getPowerupAmount(connection, "miracle");

            if (numMiracles == 0) {
                this.currentImageUpload = new EmbedImageGenerator(
                    STRS.getNoItem() + " <br> <br> " + STRS.getDailyPow()
                ).generate().getFileUpload();

                this.miraclesToUse = 0;

                Log.debug(this.user, this.getClass(), "Failed to miracle: Has none");
                this.sendResponse();
                return;
            }

            if (this.miraclesToUse > numMiracles) {
                this.currentImageUpload = new EmbedImageGenerator(
                    STRS.getSomePow().formatted(
                        this.miraclesToUse,
                        this.miraclesToUse == 1
                            ? POWS.get("miracle").getName()
                            : POWS.get("miracle").getPluralName(),
                        numMiracles
                    ) + " <br> <br> " + STRS.getDailyPow()
                ).generate().getFileUpload();

                this.miraclesToUse = 0;

                Log.debug(this.user, this.getClass(), "Failed to miracle: Not enough");
                this.sendResponse();
                return;
            }

            this.stop(StopType.FINISHED);
            boarUser.powQuery().activateMiracles(connection, this.miraclesToUse);
            this.callingObj.execute();
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to query powerups", exception);
        } catch (IOException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate not enough miracles embed", exception);
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
            super.stop(type);
            return;
        }

        if (type.equals(StopType.EXPIRED)) {
            this.deleteInteractive(true);
            Log.debug(this.user, this.getClass(), "Cancelled interactive");
        }

        Log.debug(this.user, this.getClass(), "Finished interactive");
    }

    @Override
    public ActionRow[] getCurComponents() {
        List<ItemComponent> powSelect = InteractiveUtil.makeComponents(
            this.interactionID, COMPONENTS.get("powSelect")
        );
        List<ItemComponent> submitCancelBtns = InteractiveUtil.makeComponents(
            this.interactionID, COMPONENTS.get("powSubmitBtn"), COMPONENTS.get("powCancelBtn")
        );

        Button powSubmitBtn = ((Button) submitCancelBtns.getFirst()).withDisabled(true);

        if (this.miraclesToUse > 0) {
            powSubmitBtn = powSubmitBtn.withDisabled(false);
        }

        submitCancelBtns.set(0, powSubmitBtn);

        return new ActionRow[] {
            ActionRow.of(powSelect),
            ActionRow.of(submitCancelBtns)
        };
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(modalEvent, this, e));

        PowerupItemConfig miracleConfig = POWS.get("miracle");

        Log.debug(this.user, this.getClass(), "Miracle input: " + modalEvent.getValues().getFirst().getAsString());

        try {
            try (Connection connection = DataUtil.getConnection()) {
                BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);

                int numMiracles = boarUser.powQuery().getPowerupAmount(connection, "miracle");
                String inputStr = modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                int input = inputStr.isEmpty() ? numMiracles :  Integer.parseInt(inputStr);

                if (input == 0 && numMiracles > 0) {
                    throw new NumberFormatException();
                }

                if (numMiracles == 0) {
                    this.currentImageUpload = new EmbedImageGenerator(
                        STRS.getNoItem() + " <br> <br> " + STRS.getDailyPow()
                    ).generate().getFileUpload();
                    this.miraclesToUse = 0;

                    Log.debug(this.user, this.getClass(), "Failed to miracle: Has none");
                    this.sendResponse();
                    return;
                }

                if (input > numMiracles) {
                    this.currentImageUpload = new EmbedImageGenerator(
                        STRS.getSomePow().formatted(
                            input,
                            input == 1
                                ? POWS.get("miracle").getName()
                                : POWS.get("miracle").getPluralName(),
                            numMiracles
                        ) + " <br> <br> " + STRS.getDailyPow()
                    ).generate().getFileUpload();

                    Log.debug(this.user, this.getClass(), "Failed to miracle: Not enough");
                    this.sendResponse();
                    return;
                }

                long blessings = boarUser.baseQuery().getBlessings(connection, input);
                this.miraclesToUse = input;

                this.currentImageUpload = new EmbedImageGenerator(
                    STRS.getMiracleAttempt().formatted(
                        this.miraclesToUse,
                        this.miraclesToUse == 1
                            ? miracleConfig.getName()
                            : miracleConfig.getPluralName(),
                        STRS.getBlessingsPluralName(),
                        TextUtil.getBlessHex(blessings, true),
                        STRS.getBlessingsSymbol() + " ",
                        blessings
                    )
                ).generate().getFileUpload();
            } catch (SQLException exception) {
                this.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to get powerup data", exception);
            }
        } catch (NumberFormatException exception) {
            try {
                this.currentImageUpload = new EmbedImageGenerator(STRS.getInvalidInput() + " " + STRS.getDailyPow())
                    .generate().getFileUpload();
                Log.debug(this.user, this.getClass(), "Invalid modal input");
            } catch (IOException exception1) {
                this.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to generate invalid input message", exception1);
            }
        } catch (IOException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate response message", exception);
        }

        this.sendResponse();
    }
}
