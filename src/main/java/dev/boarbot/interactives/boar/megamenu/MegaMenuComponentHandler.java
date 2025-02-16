package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import dev.boarbot.util.resource.ResourceUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

class MegaMenuComponentHandler implements Configured {
    protected static final Map<String, ModalConfig> MODALS = CONFIG.getModalConfig();

    private final MegaMenuInteractive interactive;
    private final MegaMenuActionHandler actionHandler;
    private GenericComponentInteractionCreateEvent compEvent;
    private ModalInteractionEvent modalEvent;
    private final User user;

    public MegaMenuComponentHandler(GenericComponentInteractionCreateEvent compEvent, MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.actionHandler = new MegaMenuActionHandler(interactive);
        this.compEvent = compEvent;
        this.interactive.compEvent = compEvent;
        this.user = compEvent.getUser();
    }

    public MegaMenuComponentHandler(ModalInteractionEvent modalEvent, MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.actionHandler = new MegaMenuActionHandler(interactive);
        this.modalEvent = modalEvent;
        this.interactive.modalEvent = modalEvent;
        this.user = modalEvent.getUser();
    }

    public void handleCompEvent() {
        if (this.compEvent == null) {
            return;
        }

        if (this.interactive.getModalHandler() != null) {
            this.interactive.getModalHandler().stop();
        }

        String compID = this.compEvent.getComponentId().split(",")[1];

        Set<String> modalPossibleIDs = new HashSet<>(
            Arrays.asList("PAGE", "BOAR_FIND", "INTERACT_SELECT", "POW_SELECT")
        );

        if (!modalPossibleIDs.contains(compID)) {
            this.compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.compEvent, this, e));
        }

        Log.debug(
            this.user,
            this.getClass(),
            "Page: %d | View: %s | Filtering: %b | Sorting: %b | Interacting: %b | Component: %s".formatted(
                this.interactive.page,
                this.interactive.curView,
                this.interactive.filterOpen,
                this.interactive.sortOpen,
                this.interactive.interactOpen,
                compID
            )
        );

        if (this.interactive.curView == MegaMenuView.COMPENDIUM && this.interactive.curBoarEntry != null) {
            this.interactive.mustBeExact = true;
            this.interactive.boarPage = BOARS.get(this.interactive.curBoarEntry.getKey()).getName();
            this.interactive.page = this.interactive.getFindBoarPage(this.interactive.boarPage);
            this.interactive.boarPage = null;
        }

        switch (compID) {
            case "VIEW_SELECT" -> {
                this.interactive.page = 0;

                this.interactive.prevView = this.interactive.curView;
                this.interactive.curView = MegaMenuView.fromString(
                    ((StringSelectInteractionEvent) this.compEvent).getValues().getFirst()
                );

                Log.debug(this.user, this.getClass(), "Selected view: " + this.interactive.curView);

                this.interactive.filterOpen = false;
                this.interactive.sortOpen = false;
                this.interactive.interactOpen = false;
            }

            case "LEFT" -> this.interactive.page = this.interactive.page - 1;

            case "PAGE" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("pageInput"), compEvent);
                this.interactive.setModalHandler(
                    new ModalHandler(this.compEvent, this.interactive, NUMS.getInteractiveIdle())
                );
                this.compEvent.replyModal(modal)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent page input modal");
            }

            case "RIGHT" -> this.interactive.page = this.interactive.page + 1;

            case "BOAR_FIND" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("findBoar"), compEvent);
                this.interactive.setModalHandler(
                    new ModalHandler(this.compEvent, this.interactive, NUMS.getInteractiveIdle())
                );
                this.compEvent.replyModal(modal)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent boar find input modal");
            }

            case "BOAR_INTERACT" -> {
                this.interactive.interactOpen = !this.interactive.interactOpen;
                this.interactive.filterOpen = false;
                this.interactive.sortOpen = false;
            }

            case "INTERACT_SELECT" -> this.doInteract();

            case "BOAR_FILTER" -> {
                this.interactive.filterOpen = !this.interactive.filterOpen;
                this.interactive.interactOpen = false;
                this.interactive.sortOpen = false;
            }

            case "FILTER_SELECT" -> this.setFilterBits();

            case "BOAR_SORT" -> {
                this.interactive.sortOpen = !this.interactive.sortOpen;
                this.interactive.interactOpen = false;
                this.interactive.filterOpen = false;
            }

            case "SORT_SELECT" -> this.setSortVal();

            case "CONFIRM", "ADVENT_CLAIM" -> this.interactive.getBoarUser().passSynchronizedAction(() -> {
                if (this.interactive.curView == MegaMenuView.ADVENT) {
                    this.actionHandler.claimAdvent(this.interactive.getBoarUser());
                } else if (this.interactive.powerupUsing != null) {
                    this.actionHandler.usePowerup(this.interactive.getBoarUser());
                }
            });

            case "CANCEL" -> {
                this.interactive.confirmOpen = false;
                this.interactive.interactType = null;
            }

            case "BACK" -> {
                this.interactive.acknowledgeOpen = false;
                this.interactive.acknowledgeImageGen = null;

                if (this.interactive.curView == MegaMenuView.EDITIONS) {
                    this.interactive.prevView = this.interactive.curView;
                    this.interactive.curView = MegaMenuView.COMPENDIUM;

                    String boarID = this.interactive.curBoarEntry.getKey();

                    if (this.interactive.filteredBoars.containsKey(boarID)) {
                        this.interactive.boarPage = BOARS.get(this.interactive.curBoarEntry.getKey()).getName();
                    } else {
                        this.interactive.page = 0;
                    }
                }
            }

            case "POW_SELECT" -> doPowerup();

            case "QUEST_CLAIM" -> {
                this.interactive.questAction = QuestAction.CLAIM;
                this.interactive.getBoarUser()
                    .passSynchronizedAction(() -> this.actionHandler.doQuestAction(this.interactive.getBoarUser()));
            }

            case "QUEST_BONUS" -> {
                this.interactive.questAction = QuestAction.CLAIM_BONUS;
                this.interactive.getBoarUser()
                    .passSynchronizedAction(() -> this.actionHandler.doQuestAction(this.interactive.getBoarUser()));
            }

            case "QUEST_AUTO" -> {
                this.interactive.questAction = QuestAction.AUTO_CLAIM;
                this.interactive.getBoarUser()
                    .passSynchronizedAction(() -> this.actionHandler.doQuestAction(this.interactive.getBoarUser()));
            }
        }
    }

    public void handleModalEvent() {
        this.modalEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.modalEvent, this, e));

        switch (this.modalEvent.getModalId().split(",")[2]) {
            case "PAGE_INPUT" -> {
                Log.debug(
                    this.user, this.getClass(), "Page input: " + this.modalEvent.getValues().getFirst().getAsString()
                );

                try {
                    String pageInput = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    this.interactive.page = Math.max(Integer.parseInt(pageInput)-1, 0);
                } catch (NumberFormatException exception) {
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                }
            }

            case "FIND_BOAR" -> {
                Log.debug(
                    this.user, this.getClass(), "Find input: " + this.modalEvent.getValues().getFirst().getAsString()
                );

                this.interactive.page = this.interactive
                    .getFindBoarPage(this.modalEvent.getValues().getFirst().getAsString());
            }

            case "CLONE_AMOUNT" -> {
                Log.debug(
                    this.user, this.getClass(), "Clone input: " + this.modalEvent.getValues().getFirst().getAsString()
                );

                try (Connection connection = DataUtil.getConnection()) {
                    this.interactive.numClone = this.interactive.getBoarUser().powQuery()
                        .getPowerupAmount(connection, "clone");

                    String inputStr = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    int input = inputStr.isEmpty() ? this.interactive.numClone : Integer.parseInt(inputStr);

                    if (input == 0 && this.interactive.numClone > 0) {
                        throw new NumberFormatException();
                    }

                    if (this.interactive.numClone == 0) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getNoItem());

                        Log.debug(this.user, this.getClass(), "Failed to clone: Has none");
                        this.interactive.execute(null);
                        return;
                    }

                    int avgClones = RARITIES.get(this.interactive.curRarityKey).getAvgClones();
                    boolean tooMany = input / avgClones > 25 || input / avgClones == 25 && input % avgClones > 0;

                    if (tooMany && !inputStr.isEmpty()) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                            null, STRS.getCompCloneTooMany()
                        );


                        Log.debug(this.user, this.getClass(), "Failed to clone: Too many");
                        this.interactive.execute(null);
                        return;
                    }

                    if (tooMany) {
                        input = avgClones * 25;
                    }

                    if (input > this.interactive.numClone) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                            null,
                            STRS.getSomePow().formatted(
                                input,
                                input == 1
                                    ? POWS.get("clone").getName()
                                    : POWS.get("clone").getPluralName(),
                                this.interactive.numClone
                            )
                        );

                        Log.debug(this.user, this.getClass(), "Failed to clone: Not enough");
                        this.interactive.execute(null);
                        return;
                    }

                    if (RARITIES.get(this.interactive.curRarityKey).getAvgClones() == 0) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                            null, STRS.getBadPow().formatted(POWS.get("clone").getPluralName())
                        );

                        Log.debug(this.user, this.getClass(), "Failed to clone: Has none");
                        this.interactive.execute(null);
                        return;
                    }

                    boolean hasBoar = this.interactive.getBoarUser().boarQuery().hasBoar(
                        this.interactive.curBoarEntry.getKey(), connection
                    );

                    if (hasBoar) {
                        this.confirmClone(input);
                        this.interactive.execute(null);
                        return;
                    }

                    String boarName = BOARS.get(this.interactive.curBoarEntry.getKey()).getName();

                    this.interactive.acknowledgeOpen = true;
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                        null, STRS.getCompNoBoar().formatted("<>" + this.interactive.curRarityKey + "<>" + boarName)
                    );

                    Log.debug(this.user, this.getClass(), "Failed to clone: Lack of boar");
                } catch (NumberFormatException exception) {
                    this.interactive.acknowledgeOpen = true;
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getInvalidInput());
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                } catch (SQLException exception) {
                    this.interactive.stop(StopType.EXCEPTION);
                    Log.error(this.user, this.getClass(), "Failed to get clone data", exception);
                    return;
                }
            }

            case "MIRACLE_AMOUNT" -> {
                Log.debug(
                    this.user, this.getClass(), "Miracle input: " + this.modalEvent.getValues().getFirst().getAsString()
                );

                PowerupItemConfig miracleConfig = POWS.get("miracle");

                try (Connection connection = DataUtil.getConnection()) {
                    this.interactive.powData = this.interactive.getBoarUser().megaQuery().getPowerupsData(connection);
                    int numMiracles = this.interactive.powData.powAmts().get("miracle");

                    String inputStr = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    int input = inputStr.isEmpty() ? numMiracles : Integer.parseInt(inputStr);

                    if (input == 0 && numMiracles > 0) {
                        throw new NumberFormatException();
                    }

                    if (numMiracles == 0) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getNoItem());

                        Log.debug(this.user, this.getClass(), "Failed to miracle: Has none");
                        this.interactive.execute(null);
                        return;
                    }

                    if (input > numMiracles) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                            null,
                            STRS.getSomePow().formatted(
                                input,
                                input == 1
                                    ? POWS.get("miracle").getName()
                                    : POWS.get("miracle").getPluralName(),
                                numMiracles
                            )
                        );

                        Log.debug(this.user, this.getClass(), "Failed to miracle: Not enough");
                        this.interactive.execute(null);
                        return;
                    }

                    long blessings = this.interactive.getBoarUser().baseQuery().getBlessings(connection, input);
                    this.interactive.numTryCharm = input;

                    this.interactive.confirmOpen = true;
                    this.interactive.confirmString = STRS.getMiracleAttempt().formatted(
                        this.interactive.numTryCharm,
                        this.interactive.numTryCharm == 1
                            ? miracleConfig.getName()
                            : miracleConfig.getPluralName(),
                        STRS.getBlessingsPluralName(),
                        TextUtil.getBlessHex(blessings, true),
                        STRS.getBlessingsSymbol() + " ",
                        blessings
                    );
                } catch (NumberFormatException exception) {
                    this.interactive.acknowledgeOpen = true;
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getInvalidInput());
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                } catch (SQLException exception) {
                    this.interactive.stop(StopType.EXCEPTION);
                    Log.error(this.user, this.getClass(), "Failed to get miracle data", exception);
                    return;
                }
            }
        }

        this.interactive.execute(null);
    }

    private void doInteract() {
        this.interactive.interactType = InteractType.fromString(
            ((StringSelectInteractionEvent) this.compEvent).getValues().getFirst()
        );

        Log.debug(this.user, this.getClass(), "Interact type: " + this.interactive.interactType);

        switch (this.interactive.interactType) {
            case FAVORITE -> {
                this.compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.compEvent, this, e));
                this.interactive.getBoarUser()
                    .passSynchronizedAction(() -> this.actionHandler.doInteract(this.interactive.getBoarUser()));
            }

            case CLONE -> {
                Modal modal = ModalUtil.getModal(MODALS.get("cloneAmount"), this.compEvent);
                this.interactive.setModalHandler(
                    new ModalHandler(this.compEvent, this.interactive, NUMS.getInteractiveIdle())
                );
                this.compEvent.replyModal(modal)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent clone input modal");
            }

            case TRANSMUTE -> {
                this.compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.compEvent, this, e));
                this.interactive.confirmOpen = true;

                String nextRarityKey = BoarUtil.getNextRarityKey(this.interactive.curRarityKey);
                String boarPluralName = BOARS.get(this.interactive.curBoarEntry.getKey()).getPluralName();

                this.interactive.confirmString = STRS.getCompTransmuteConfirm().formatted(
                    "<>" + this.interactive.curRarityKey + "<>" + boarPluralName,
                    "<>" + nextRarityKey + "<>" + RARITIES.get(nextRarityKey).getName()
                );
            }

            case EDITIONS -> {
                this.compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.compEvent, this, e));

                this.interactive.page = 0;

                this.interactive.prevView = this.interactive.curView;
                this.interactive.curView = MegaMenuView.EDITIONS;

                this.interactive.filterOpen = false;
                this.interactive.sortOpen = false;
                this.interactive.interactOpen = false;
            }

            case ZOOM -> {
                this.compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.compEvent, this, e));
                this.interactive.acknowledgeOpen = true;

                String curBoarID = this.interactive.curBoarEntry.getKey();
                BoarItemConfig curBoar = BOARS.get(curBoarID);

                if (curBoar.getStaticFile() == null) {
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                        null, BoarBotApp.getBot().getImageCacheMap().get("large" + curBoarID)
                    );
                    return;
                }

                String filePath = ResourceUtil.boarAssetsPath + curBoar.getFile();

                try {
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                        null, filePath, NUMS.getLargeBoarSize()
                    );
                } catch (IOException | URISyntaxException exception) {
                    this.interactive.stop(StopType.EXCEPTION);
                    Log.error(this.user, this.getClass(), "Failed to get animated overlay", exception);
                }
            }

            case null -> {}
        }
    }

    private void doPowerup() {
        this.interactive.powerupUsing = ((StringSelectInteractionEvent) this.compEvent).getValues().getFirst();
        PowerupItemConfig powConfig = POWS.get(this.interactive.powerupUsing);

        switch (this.interactive.powerupUsing) {
            case "miracle" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("miracleAmount"), compEvent);
                this.interactive.setModalHandler(
                    new ModalHandler(this.compEvent, this.interactive, NUMS.getInteractiveIdle())
                );
                this.compEvent.replyModal(modal)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent miracle input modal");
            }

            case "gift" -> {
                this.compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.compEvent, this, e));

                this.interactive.confirmOpen = true;
                this.interactive.confirmString = STRS.getPowGiftConfirm().formatted(powConfig.getName());
            }

            case "clone", "transmute" -> {
                this.compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(this.compEvent, this, e));

                this.interactive.acknowledgeOpen = true;
                this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getPowCannotUse().formatted(powConfig.getPluralName())
                );
            }
        }
    }

    private void setFilterBits() {
        this.interactive.page = 0;

        List<String> values = ((StringSelectInteractionEvent) this.compEvent).getValues();
        int filterBits = 0;

        for (String value : values) {
            filterBits += Integer.parseInt(value);
        }

        this.interactive.filterBits = filterBits;

        try {
            BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());
            interBoarUser.passSynchronizedAction(() -> this.actionHandler.setFilterBits(interBoarUser));
        } catch (SQLException exception) {
            this.interactive.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update data", exception);
        }
    }

    private void setSortVal() {
        this.interactive.page = 0;

        this.interactive.sortVal = SortType.values()[
            Integer.parseInt(((StringSelectInteractionEvent) this.compEvent).getValues().getFirst())
        ];

        try {
            BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());
            interBoarUser.passSynchronizedAction(() -> this.actionHandler.setSortVal(interBoarUser));
        } catch (SQLException exception) {
            this.interactive.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update data", exception);
        }
    }

    private void confirmClone(int input) {
        RarityConfig rarity = RARITIES.get(this.interactive.curRarityKey);
        int avgClones = rarity.getAvgClones();

        this.interactive.numTryClone = input;
        this.interactive.confirmOpen = true;

        double percentVal = ((double) (input % avgClones) / avgClones) * 100;
        NumberFormat percentFormat = new DecimalFormat("#.##");

        String boarName = input / avgClones > 1
            ? BOARS.get(this.interactive.curBoarEntry.getKey()).getPluralName()
            : BOARS.get(this.interactive.curBoarEntry.getKey()).getName();
        String cloneName = input == 1
            ? POWS.get("clone").getName()
            : POWS.get("clone").getPluralName();
        String percentStr = percentVal == 0
            ? percentFormat.format(percentVal + 100) + "%"
            : percentFormat.format(percentVal) + "%";

        if (input % avgClones == 0) {
            this.interactive.confirmString = STRS.getCompCloneConfirmClean().formatted(
                input, cloneName, input / avgClones, "<>" + this.interactive.curRarityKey + "<>" + boarName
            );
        } else if (input / avgClones < 1) {
            this.interactive.confirmString = STRS.getCompCloneConfirmOne().formatted(
                input, cloneName, percentStr, "<>" + this.interactive.curRarityKey + "<>" + boarName
            );
        } else {
            this.interactive.confirmString = STRS.getCompCloneConfirmMultiple().formatted(
                input, cloneName, input / avgClones, "<>" + this.interactive.curRarityKey + "<>" + boarName, percentStr
            );
        }
    }
}
