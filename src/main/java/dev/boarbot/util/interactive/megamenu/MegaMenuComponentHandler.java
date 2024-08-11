package dev.boarbot.util.interactive.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.boar.megamenu.MegaMenuInteractive;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.modals.megamenu.CloneModalHandler;
import dev.boarbot.modals.megamenu.FindBoarModalHandler;
import dev.boarbot.modals.PageInputModalHandler;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.modal.ModalUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

import java.sql.SQLException;
import java.util.List;

@Log4j2
public class MegaMenuComponentHandler {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    private GenericComponentInteractionCreateEvent compEvent = null;
    private ModalInteractionEvent modalEvent = null;
    private final MegaMenuInteractive interactive;

    public MegaMenuComponentHandler(GenericComponentInteractionCreateEvent compEvent, MegaMenuInteractive interactive) {
        this.compEvent = compEvent;
        this.interactive = interactive;
    }

    public MegaMenuComponentHandler(ModalInteractionEvent modalEvent, MegaMenuInteractive interactive) {
        this.modalEvent = modalEvent;
        this.interactive = interactive;
    }

    public void handleCompEvent() {
        if (this.compEvent != null) {
            if (!this.interactive.getInitEvent().getUser().getId().equals(compEvent.getUser().getId())) {
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

                case "OKAY" -> this.interactive.setAcknowledgeOpen(false);
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
                // TODO
                // Edit image with confirmation
                // Remove all buttons and replace with Confirm or Cancel
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
                this.interactive.getInitEvent().getHook().editOriginalComponents(
                    this.interactive.getCurComponents()
                ).complete();
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
            BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getInitEvent().getUser());
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
            BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getInitEvent().getUser());
            interBoarUser.passSynchronizedAction(this.interactive);
            interBoarUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to get boar user.", exception);
        }
    }
}
