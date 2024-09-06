package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.bot.config.items.ItemConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.modals.megamenu.CloneModalHandler;
import dev.boarbot.modals.megamenu.FindBoarModalHandler;
import dev.boarbot.modals.PageInputModalHandler;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.modal.ModalUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

@Log4j2
class MegaMenuComponentHandler {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    private final MegaMenuInteractive interactive;
    private GenericComponentInteractionCreateEvent compEvent;
    private ModalInteractionEvent modalEvent;

    public MegaMenuComponentHandler(GenericComponentInteractionCreateEvent compEvent, MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.compEvent = compEvent;
        this.interactive.setCompEvent(compEvent);
    }

    public MegaMenuComponentHandler(ModalInteractionEvent modalEvent, MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.modalEvent = modalEvent;
        this.interactive.setModalEvent(modalEvent);
    }

    public void handleCompEvent() {
        if (this.compEvent != null) {
            if (!this.interactive.getUser().getId().equals(compEvent.getUser().getId())) {
                this.compEvent.deferEdit().queue();
                return;
            }

            if (this.interactive.getModalHandler() != null) {
                this.interactive.getModalHandler().stop();
            }

            String compID = this.compEvent.getComponentId().split(",")[1];

            if (!compID.equals("PAGE") && !compID.equals("BOAR_FIND") && !compID.equals("INTERACT_SELECT")) {
                this.compEvent.deferEdit().queue();
            }

            switch (compID) {
                case "VIEW_SELECT" -> {
                    this.setPageZero();

                    this.interactive.setPrevView(this.interactive.getCurView());
                    this.interactive.setCurView(MegaMenuView.fromString(
                        ((StringSelectInteractionEvent) this.compEvent).getValues().getFirst()
                    ));

                    this.interactive.setFilterOpen(false);
                    this.interactive.setSortOpen(false);
                    this.interactive.setInteractOpen(false);
                }

                case "LEFT" -> {
                    this.interactive.setPrevPage(this.interactive.getPage());
                    this.interactive.setPage(this.interactive.getPage() - 1);
                }

                case "PAGE" -> {
                    Modal modal = this.makeModal(
                        this.config.getModalConfig().get("pageInput")
                    );

                    this.interactive.setModalHandler(new PageInputModalHandler(this.compEvent, this.interactive));
                    this.compEvent.replyModal(modal).complete();
                }

                case "RIGHT" -> {
                    this.interactive.setPrevPage(this.interactive.getPage());
                    this.interactive.setPage(this.interactive.getPage() + 1);
                }

                case "BOAR_FIND" -> {
                    Modal modal = this.makeModal(
                        this.config.getModalConfig().get("findBoar")
                    );

                    this.interactive.setModalHandler(new FindBoarModalHandler(this.compEvent, this.interactive));
                    this.compEvent.replyModal(modal).complete();
                }

                case "BOAR_INTERACT" -> {
                    this.interactive.setInteractOpen(!this.interactive.isInteractOpen());
                    this.interactive.setFilterOpen(false);
                    this.interactive.setSortOpen(false);
                }

                case "INTERACT_SELECT" -> this.doInteract();

                case "BOAR_FILTER" -> {
                    this.interactive.setFilterOpen(!this.interactive.isFilterOpen());
                    this.interactive.setInteractOpen(false);
                    this.interactive.setSortOpen(false);
                }

                case "FILTER_SELECT" -> this.setFilterBits();

                case "BOAR_SORT" -> {
                    this.interactive.setSortOpen(!this.interactive.isSortOpen());
                    this.interactive.setInteractOpen(false);
                    this.interactive.setFilterOpen(false);
                }

                case "SORT_SELECT" -> this.setSortVal();

                case "CONFIRM" -> this.interactive.getBoarUser().passSynchronizedAction(this.interactive);

                case "CANCEL" -> {
                    this.interactive.setConfirmOpen(false);
                    this.interactive.setInteractType(null);
                }

                case "BACK" -> {
                    this.interactive.setAcknowledgeOpen(false);
                    this.interactive.setAcknowledgeImageGen(null);

                    if (this.interactive.getCurView() == MegaMenuView.EDITIONS) {
                        this.interactive.setPrevView(this.interactive.getCurView());
                        this.interactive.setCurView(MegaMenuView.COMPENDIUM);
                        this.interactive.setBoarPage(this.interactive.getCurBoarEntry().getKey());
                    }
                }
            }
        }
    }

    public void handleModalEvent() {
        this.modalEvent.deferEdit().complete();

        switch (this.modalEvent.getModalId().split(",")[2]) {
            case "PAGE_INPUT" -> {
                try {
                    String pageInput = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    this.interactive.setPrevPage(this.interactive.getPage());
                    this.interactive.setPage(Math.max(Integer.parseInt(pageInput)-1, 0));
                    this.interactive.execute(null);
                } catch (NumberFormatException ignore) {}
            }

            case "FIND_BOAR" -> {
                this.interactive.setPrevPage(this.interactive.getPage());
                this.interactive.setPage(
                    this.interactive.getFindBoarPage(this.modalEvent.getValues().getFirst().getAsString())
                );
                this.interactive.execute(null);
            }

            case "CLONE_AMOUNT" -> {
                StringConfig strConfig = this.config.getStringConfig();

                try (Connection connection = DataUtil.getConnection()) {
                    this.interactive.setNumClone(this.interactive.getBoarUser().getPowerupAmount(connection, "clone"));

                    String inputStr = this.modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    int input = Math.min(Math.max(Integer.parseInt(inputStr), 1), this.interactive.getNumClone());

                    int avgClones = this.config.getRarityConfigs().get(
                        this.interactive.getCurRarityKey()
                    ).getAvgClones();

                    boolean cloneable = avgClones != 0 && input > 0;

                    if (cloneable) {
                        boolean hasBoar = this.interactive.getBoarUser().hasBoar(
                            this.interactive.getCurBoarEntry().getKey(), connection
                        );

                        if (hasBoar) {
                            this.confirmClone(input);
                        } else {
                            String boarName = this.config.getItemConfig().getBoars().get(
                                this.interactive.getCurBoarEntry().getKey()
                            ).getName();

                            this.interactive.setAcknowledgeOpen(true);
                            this.interactive.setAcknowledgeImageGen(
                                new OverlayImageGenerator(
                                    null,
                                    strConfig.getCompNoBoar().formatted(
                                        "<>" + this.interactive.getCurRarityKey() + "<>" + boarName
                                    )
                                )
                            );
                        }
                    } else {
                        this.interactive.setAcknowledgeOpen(true);
                        this.interactive.setAcknowledgeImageGen(
                            new OverlayImageGenerator(
                                null,
                                strConfig.getCompNoPow().formatted(
                                    this.config.getItemConfig().getPowerups().get("clone").getPluralName()
                                )
                            )
                        );
                    }
                } catch (NumberFormatException exception1) {
                    this.interactive.setAcknowledgeOpen(true);
                    this.interactive.setAcknowledgeImageGen(
                        new OverlayImageGenerator(null, strConfig.getInvalidInput())
                    );
                } catch (SQLException exception2) {
                    log.error("Failed to get user data", exception2);
                } finally {
                    this.interactive.execute(null);
                }
            }
        }
    }

    private void setPageZero() {
        this.interactive.setPrevPage(this.interactive.getPage());
        this.interactive.setPage(0);
    }

    private Modal makeModal(ModalConfig modalConfig) {
        return new ModalImpl(
            ModalUtil.makeModalID(modalConfig.getId(), this.compEvent),
            modalConfig.getTitle(),
            ModalUtil.makeModalComponents(modalConfig.getComponents())
        );
    }

    private void doInteract() {
        this.interactive.setInteractType(InteractType.fromString(
            ((StringSelectInteractionEvent) this.compEvent).getValues().getFirst()
        ));

        switch (this.interactive.getInteractType()) {
            case FAVORITE -> {
                this.compEvent.deferEdit().queue();
                this.interactive.getBoarUser().passSynchronizedAction(this.interactive);
            }

            case CLONE -> {
                ModalConfig modalConfig = this.config.getModalConfig().get("cloneAmount");

                Modal modal = new ModalImpl(
                    ModalUtil.makeModalID(modalConfig.getId(), this.compEvent),
                    modalConfig.getTitle(),
                    ModalUtil.makeModalComponents(modalConfig.getComponents())
                );

                this.interactive.setModalHandler(new CloneModalHandler(this.compEvent, this.interactive));

                if (this.interactive.isStopped()) {
                    return;
                }

                this.compEvent.replyModal(modal).complete();
            }

            case TRANSMUTE -> {
                this.compEvent.deferEdit().queue();
                this.interactive.setConfirmOpen(true);

                String nextRarityKey = BoarUtil.getNextRarityKey(this.interactive.getCurRarityKey());

                String boarPluralName = this.config.getItemConfig().getBoars().get(
                    this.interactive.getCurBoarEntry().getKey()
                ).getPluralName();

                this.interactive.setConfirmString(this.config.getStringConfig().getCompTransmuteConfirm().formatted(
                    "<>" + this.interactive.getCurRarityKey() + "<>" + boarPluralName,
                    "<>" + nextRarityKey + "<>" + this.config.getRarityConfigs().get(nextRarityKey).getName()
                ));
            }

            case EDITIONS -> {
                this.compEvent.deferEdit().queue();

                this.setPageZero();

                this.interactive.setPrevView(this.interactive.getCurView());
                this.interactive.setCurView(MegaMenuView.EDITIONS);

                this.interactive.setFilterOpen(false);
                this.interactive.setSortOpen(false);
                this.interactive.setInteractOpen(false);
            }

            case ZOOM -> {
                this.compEvent.deferEdit().queue();
                this.interactive.setAcknowledgeOpen(true);

                String curBoarID = this.interactive.getCurBoarEntry().getKey();
                BoarItemConfig curBoar = this.config.getItemConfig().getBoars().get(curBoarID);

                if (curBoar.getStaticFile() != null) {
                    String filePath = this.config.getPathConfig().getBoars() + curBoar.getFile();

                    this.interactive.setAcknowledgeImageGen(
                        new OverlayImageGenerator(
                            null, filePath, this.config.getNumberConfig().getLargeBoarSize()
                        )
                    );
                } else {
                    this.interactive.setAcknowledgeImageGen(
                        new OverlayImageGenerator(null, BoarBotApp.getBot().getImageCacheMap().get("large" + curBoarID))
                    );
                }
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

        this.interactive.setFilterBits(filterBits);

        try {
            BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());
            interBoarUser.passSynchronizedAction(this.interactive);
            interBoarUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to get boar user.", exception);
        }
    }

    private void setSortVal() {
        this.setPageZero();

        this.interactive.setSortVal(SortType.values()[
            Integer.parseInt(((StringSelectInteractionEvent) this.compEvent).getValues().getFirst())
        ]);

        try {
            BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());
            interBoarUser.passSynchronizedAction(this.interactive);
            interBoarUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to get boar user.", exception);
        }
    }

    private void confirmClone(int input) {
        ItemConfig itemConfig = this.config.getItemConfig();
        StringConfig strConfig = this.config.getStringConfig();

        RarityConfig rarity = this.config.getRarityConfigs().get(this.interactive.getCurRarityKey());
        int avgClones = rarity.getAvgClones();

        boolean tooMany = input / avgClones > 25 || input / avgClones == 25 && input % avgClones > 0;

        if (tooMany) {
            input = avgClones * 25;
        }

        this.interactive.setNumTryClone(input);
        this.interactive.setConfirmOpen(true);

        double percentVal = ((double) (input % avgClones) / avgClones) * 100;
        NumberFormat percentFormat = new DecimalFormat("#.##");

        if (input / avgClones <= 1) {
            String boarName = itemConfig.getBoars().get(
                this.interactive.getCurBoarEntry().getKey()
            ).getName();
            String cloneName = input == 1
                ? itemConfig.getPowerups().get("clone").getName()
                : itemConfig.getPowerups().get("clone").getPluralName();
            String percentStr = percentVal == 0
                ? percentFormat.format(percentVal + 100) + "%"
                : percentFormat.format(percentVal) + "%";

            this.interactive.setConfirmString(strConfig.getCompCloneConfirmOne().formatted(
                "%,d".formatted(input) + " " + cloneName,
                percentStr,
                "<>" + this.interactive.getCurRarityKey() + "<>" + boarName
            ));
        } else {
            String boarName = input / avgClones > 1
                ? itemConfig.getBoars().get(
                    this.interactive.getCurBoarEntry().getKey()
                ).getPluralName()
                : itemConfig.getBoars().get(
                    this.interactive.getCurBoarEntry().getKey()
                ).getName();

            this.interactive.setConfirmString(strConfig.getCompCloneConfirmMultiple().formatted(
                "%,d".formatted(input) + " " + itemConfig.getPowerups().get("clone").getPluralName(),
                (input / avgClones),
                "<>" + this.interactive.getCurRarityKey() + "<>" + boarName,
                percentFormat.format(percentVal) + "%"
            ));
        }
    }
}
