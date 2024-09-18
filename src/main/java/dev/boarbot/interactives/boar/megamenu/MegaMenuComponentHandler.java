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
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

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
    private GenericComponentInteractionCreateEvent compEvent;
    private ModalInteractionEvent modalEvent;
    private User user;

    public MegaMenuComponentHandler(GenericComponentInteractionCreateEvent compEvent, MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.compEvent = compEvent;
        this.interactive.compEvent = compEvent;
        this.user = compEvent.getUser();
    }

    public MegaMenuComponentHandler(ModalInteractionEvent modalEvent, MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.modalEvent = modalEvent;
        this.interactive.modalEvent = modalEvent;
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
            this.compEvent.deferEdit().queue();
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

        switch (compID) {
            case "VIEW_SELECT" -> {
                this.setPageZero();

                this.interactive.prevView = this.interactive.curView;
                this.interactive.curView = MegaMenuView.fromString(
                    ((StringSelectInteractionEvent) this.compEvent).getValues().getFirst()
                );

                Log.debug(this.user, this.getClass(), "Selected view: " + this.interactive.curView);

                this.interactive.filterOpen = false;
                this.interactive.sortOpen = false;
                this.interactive.interactOpen = false;
            }

            case "LEFT" -> {
                this.interactive.prevPage = this.interactive.page;
                this.interactive.page = this.interactive.page - 1;
            }

            case "PAGE" -> {
                Modal modal = this.makeModal(MODALS.get("pageInput"));

                this.interactive.setModalHandler(new ModalHandler(this.compEvent, this.interactive));
                this.compEvent.replyModal(modal).queue();
                Log.debug(this.user, this.getClass(), "Sent page input modal");
            }

            case "RIGHT" -> {
                this.interactive.prevPage = this.interactive.page;
                this.interactive.page = this.interactive.page + 1;
            }

            case "BOAR_FIND" -> {
                Modal modal = this.makeModal(MODALS.get("findBoar"));

                this.interactive.setModalHandler(new ModalHandler(this.compEvent, this.interactive));
                this.compEvent.replyModal(modal).queue();
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

            case "CONFIRM" -> this.interactive.getBoarUser().passSynchronizedAction(this.interactive);

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
                    this.interactive.boarPage = this.interactive.curBoarEntry.getKey();
                }
            }

            case "POW_SELECT" -> doPowerup();

            case "QUEST_CLAIM" -> {
                this.interactive.questAction = QuestAction.CLAIM;
                this.interactive.getBoarUser().passSynchronizedAction(this.interactive);
            }

            case "QUEST_BONUS" -> {
                this.interactive.questAction = QuestAction.CLAIM_BONUS;
                this.interactive.getBoarUser().passSynchronizedAction(this.interactive);
            }

            case "QUEST_AUTO" -> {
                this.interactive.questAction = QuestAction.AUTO_CLAIM;
                this.interactive.getBoarUser().passSynchronizedAction(this.interactive);
            }
        }
    }

    public void handleModalEvent() {
        this.modalEvent.deferEdit().queue();

        switch (this.modalEvent.getModalId().split(",")[2]) {
            case "PAGE_INPUT" -> {
                Log.debug(
                    this.user, this.getClass(), "Page input: " + this.modalEvent.getValues().getFirst().getAsString()
                );

                try {
                    String pageInput = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    this.interactive.prevPage = this.interactive.page;
                    this.interactive.page = Math.max(Integer.parseInt(pageInput)-1, 0);
                } catch (NumberFormatException exception) {
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                } finally {
                    this.interactive.execute(null);
                }
            }

            case "FIND_BOAR" -> {
                Log.debug(
                    this.user, this.getClass(), "Find input: " + this.modalEvent.getValues().getFirst().getAsString()
                );

                this.interactive.prevPage = this.interactive.page;
                this.interactive.page = this.interactive
                    .getFindBoarPage(this.modalEvent.getValues().getFirst().getAsString());
                this.interactive.execute(null);
            }

            case "CLONE_AMOUNT" -> {
                Log.debug(
                    this.user, this.getClass(), "Clone input: " + this.modalEvent.getValues().getFirst().getAsString()
                );

                try (Connection connection = DataUtil.getConnection()) {
                    this.interactive.numClone = this.interactive.getBoarUser().powQuery()
                        .getPowerupAmount(connection, "clone");

                    String inputStr = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    int input = Math.min(Integer.parseInt(inputStr), this.interactive.numClone);

                    if (input == 0) {
                        throw new NumberFormatException();
                    }

                    if (RARITIES.get(this.interactive.curRarityKey).getAvgClones() > this.interactive.numClone) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                            null, STRS.getNoPow().formatted(POWS.get("clone").getPluralName())
                        );

                        Log.debug(this.user, this.getClass(), "Failed to clone: Not enough");
                        return;
                    }

                    boolean hasBoar = this.interactive.getBoarUser().boarQuery().hasBoar(
                        this.interactive.curBoarEntry.getKey(), connection
                    );

                    if (hasBoar) {
                        this.confirmClone(input);
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
                    Log.error(this.user, this.getClass(), "Failed to get clone data", exception);
                } finally {
                    this.interactive.execute(null);
                }
            }

            case "MIRACLE_AMOUNT" -> {
                PowerupItemConfig miracleConfig = POWS.get("miracle");

                try (Connection connection = DataUtil.getConnection()) {
                    this.interactive.powData = this.interactive.getBoarUser().megaQuery().getPowerupsData(connection);
                    int numMiracles = this.interactive.powData.powAmts().get("miracle");

                    String inputStr = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    int input = Math.min(Integer.parseInt(inputStr), numMiracles);

                    if (input <= 0) {
                        throw new NumberFormatException();
                    }

                    if (numMiracles == 0) {
                        this.interactive.acknowledgeOpen = true;
                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                            null, STRS.getNoPow().formatted(POWS.get("miracle").getPluralName())
                        );

                        Log.debug(this.user, this.getClass(), "Failed to miracle: Not enough");
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
                        TextUtil.getBlessHex(blessings),
                        blessings > 1000
                            ? STRS.getBlessingsSymbol() + " "
                            : "",
                        blessings
                    );
                } catch (NumberFormatException exception) {
                    this.interactive.acknowledgeOpen = true;
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getInvalidInput());
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                } catch (SQLException exception) {
                    Log.error(this.user, this.getClass(), "Failed to get miracle data", exception);
                } finally {
                    this.interactive.execute(null);
                }
            }
        }
    }

    private void setPageZero() {
        this.interactive.prevPage = this.interactive.page;
        this.interactive.page = 0;
    }

    private Modal makeModal(ModalConfig modalConfig) {
        return new ModalImpl(
            ModalUtil.makeModalID(modalConfig.getId(), this.compEvent),
            modalConfig.getTitle(),
            ModalUtil.makeModalComponents(modalConfig.getComponents())
        );
    }

    private void doInteract() {
        this.interactive.interactType = InteractType.fromString(
            ((StringSelectInteractionEvent) this.compEvent).getValues().getFirst()
        );

        Log.debug(this.user, this.getClass(), "Interact type: " + this.interactive.interactType);

        switch (this.interactive.interactType) {
            case FAVORITE -> {
                this.compEvent.deferEdit().queue();
                this.interactive.getBoarUser().passSynchronizedAction(this.interactive);
            }

            case CLONE -> {
                ModalConfig curModalConfig = MODALS.get("cloneAmount");

                Modal modal = new ModalImpl(
                    ModalUtil.makeModalID(curModalConfig.getId(), this.compEvent),
                        curModalConfig.getTitle(),
                    ModalUtil.makeModalComponents(curModalConfig.getComponents())
                );

                this.interactive.setModalHandler(new ModalHandler(this.compEvent, this.interactive));

                if (this.interactive.isStopped()) {
                    return;
                }

                this.compEvent.replyModal(modal).queue();
                Log.debug(this.user, this.getClass(), "Sent clone input modal");
            }

            case TRANSMUTE -> {
                this.compEvent.deferEdit().queue();
                this.interactive.confirmOpen = true;

                String nextRarityKey = BoarUtil.getNextRarityKey(this.interactive.curRarityKey);
                String boarPluralName = BOARS.get(this.interactive.curBoarEntry.getKey()).getPluralName();

                this.interactive.confirmString = STRS.getCompTransmuteConfirm().formatted(
                    "<>" + this.interactive.curRarityKey + "<>" + boarPluralName,
                    "<>" + nextRarityKey + "<>" + RARITIES.get(nextRarityKey).getName()
                );
            }

            case EDITIONS -> {
                this.compEvent.deferEdit().queue();

                this.setPageZero();

                this.interactive.prevView = this.interactive.curView;
                this.interactive.curView = MegaMenuView.EDITIONS;

                this.interactive.filterOpen = false;
                this.interactive.sortOpen = false;
                this.interactive.interactOpen = false;
            }

            case ZOOM -> {
                this.compEvent.deferEdit().queue();
                this.interactive.acknowledgeOpen = true;

                String curBoarID = this.interactive.curBoarEntry.getKey();
                BoarItemConfig curBoar = BOARS.get(curBoarID);

                if (curBoar.getStaticFile() == null) {
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                        null, BoarBotApp.getBot().getImageCacheMap().get("large" + curBoarID)
                    );
                    return;
                }

                String filePath = PATHS.getBoars() + curBoar.getFile();

                try {
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                        null, filePath, NUMS.getLargeBoarSize()
                    );
                } catch (IOException | URISyntaxException exception) {
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
                ModalConfig curModalConfig = MODALS.get("miracleAmount");

                Modal modal = new ModalImpl(
                    ModalUtil.makeModalID(curModalConfig.getId(), this.compEvent),
                    curModalConfig.getTitle(),
                    ModalUtil.makeModalComponents(curModalConfig.getComponents())
                );

                this.interactive.setModalHandler(new ModalHandler(this.compEvent, this.interactive));
                this.compEvent.replyModal(modal).queue();
                Log.debug(this.user, this.getClass(), "Sent miracle input modal");
            }

            case "gift" -> {
                this.compEvent.deferEdit().queue();

                this.interactive.confirmOpen = true;
                this.interactive.confirmString = STRS.getPowGiftConfirm().formatted(powConfig.getName());
            }

            case "clone", "transmute" -> {
                this.compEvent.deferEdit().queue();

                this.interactive.acknowledgeOpen = true;
                this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getPowCannotUse().formatted(powConfig.getPluralName())
                );
            }
        }
    }

    private void setFilterBits() {
        this.setPageZero();

        List<String> values = ((StringSelectInteractionEvent) this.compEvent).getValues();
        int filterBits = 0;

        for (String value : values) {
            filterBits += Integer.parseInt(value);
        }

        this.interactive.filterBits = filterBits;

        BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());
        interBoarUser.passSynchronizedAction(this.interactive);
    }

    private void setSortVal() {
        this.setPageZero();

        this.interactive.sortVal = SortType.values()[
            Integer.parseInt(((StringSelectInteractionEvent) this.compEvent).getValues().getFirst())
        ];

        BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());
        interBoarUser.passSynchronizedAction(this.interactive);
    }

    private void confirmClone(int input) {
        RarityConfig rarity = RARITIES.get(this.interactive.curRarityKey);
        int avgClones = rarity.getAvgClones();

        boolean tooMany = input / avgClones > 25 || input / avgClones == 25 && input % avgClones > 0;

        if (tooMany) {
            input = avgClones * 25;
        }

        this.interactive.numTryClone = input;
        this.interactive.confirmOpen = true;

        double percentVal = ((double) (input % avgClones) / avgClones) * 100;
        NumberFormat percentFormat = new DecimalFormat("#.##");

        if (input / avgClones <= 1) {
            String boarName = BOARS.get(this.interactive.curBoarEntry.getKey()).getName();
            String cloneName = input == 1
                ? POWS.get("clone").getName()
                : POWS.get("clone").getPluralName();
            String percentStr = percentVal == 0
                ? percentFormat.format(percentVal + 100) + "%"
                : percentFormat.format(percentVal) + "%";

            this.interactive.confirmString = STRS.getCompCloneConfirmOne().formatted(
                "%,d".formatted(input) + " " + cloneName,
                percentStr,
                "<>" + this.interactive.curRarityKey + "<>" + boarName
            );
        } else {
            String boarName = input / avgClones > 1
                ? BOARS.get(this.interactive.curBoarEntry.getKey()).getPluralName()
                : BOARS.get(this.interactive.curBoarEntry.getKey()).getName();

            this.interactive.confirmString = STRS.getCompCloneConfirmMultiple().formatted(
                "%,d".formatted(input) + " " + POWS.get("clone").getPluralName(),
                (input / avgClones),
                "<>" + this.interactive.curRarityKey + "<>" + boarName,
                percentFormat.format(percentVal) + "%"
            );
        }
    }
}
