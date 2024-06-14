package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.modals.PageInputModalHandler;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.megamenu.CollectionImageGenerator;
import dev.boarbot.util.generators.megamenu.MegaMenuGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.modal.ModalUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class MegaMenuInteractive extends ModalInteractive {
    private int page;
    private MegaMenuView curView;
    private ActionRow[] curComponents = new ActionRow[0];
    private ModalHandler modalHandler = null;
    private List<SelectOption> navOptions = new ArrayList<>();

    private final BoarUser boarUser;

    private final Map<MegaMenuView, Boolean> viewsToUpdateData = new HashMap<>();
    private int prevPage = -1;
    private MegaMenuView prevView;
    private FileUpload currentImage;

    private final static DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
        .appendPattern("MMMM d, yyyy")
        .toFormatter();
    private String firstJoinedDate;
    private List<String> badgeIDs;
    private Map<String, BoarInfo> boarInfos;

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getMegaMenu();

    public MegaMenuInteractive(SlashCommandInteractionEvent initEvent, MegaMenuView curView) throws SQLException {
        super(initEvent);

        this.page = initEvent.getOption("page") != null
            ? Math.max(initEvent.getOption("page").getAsInt() - 1, 0)
            : 0;
        this.boarUser = initEvent.getOption("user") != null
            ? BoarUserFactory.getBoarUser(initEvent.getOption("user").getAsUser())
            : BoarUserFactory.getBoarUser(initEvent.getUser());

        this.curView = curView;

        this.makeSelectOptions(this.COMPONENTS.get("viewSelect").getOptions());
    }

    @Override
    public void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        this.attemptExecute(compEvent, null, startTime);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent != null) {
            if (!this.initEvent.getUser().getId().equals(compEvent.getUser().getId())) {
                compEvent.deferEdit().queue();
                return;
            }

            if (this.modalHandler != null) {
                this.modalHandler.stop();
            }

            String compID = compEvent.getComponentId().split(",")[1];

            if (!compID.equals("PAGE")) {
                compEvent.deferEdit().queue();
            }

            switch (compID) {
                case "VIEW_SELECT" -> {
                    this.prevPage = this.page;
                    this.page = 0;

                    this.prevView = this.curView;
                    this.curView = MegaMenuView.fromString(
                        ((StringSelectInteractionEvent) compEvent).getValues().getFirst()
                    );
                }

                case "LEFT" -> {
                    this.prevPage = this.page;
                    this.page--;
                }

                case "PAGE" -> {
                    ModalConfig modalConfig = this.config.getModalConfig().get("pageInput");

                    Modal modal = new ModalImpl(
                        ModalUtil.makeModalID(modalConfig.getId(), compEvent),
                        modalConfig.getTitle(),
                        ModalUtil.makeModalComponents(modalConfig.getComponents())
                    );

                    this.modalHandler = new PageInputModalHandler(compEvent, this);
                    compEvent.replyModal(modal).complete();
                }

                case "RIGHT" -> {
                    this.prevPage = this.page;
                    this.page++;
                }
            }
        }

        try {
            try (Connection connection = DataUtil.getConnection()) {
                boolean shouldUpdateData = this.currentImage == null ||
                    this.lastEndTime <= this.boarUser.getLastChanged(connection);

                if (shouldUpdateData) {
                    long firstJoinedTimestamp = this.boarUser.getFirstJoinedTimestamp(connection);

                    this.firstJoinedDate = this.boarUser.getFirstJoinedTimestamp(connection) > 0
                        ? Instant.ofEpochMilli(firstJoinedTimestamp)
                            .atOffset(ZoneOffset.UTC)
                            .format(MegaMenuInteractive.dateFormatter)
                        : this.config.getStringConfig().getUnavailable();

                    this.badgeIDs = boarUser.getCurrentBadges(connection);
                    this.viewsToUpdateData.replaceAll((k, v) -> true);
                }

                if (this.prevPage == this.page && this.prevView == this.curView) {
                    return;
                }
            }

            MegaMenuGenerator imageGen = switch (this.curView) {
                case MegaMenuView.PROFILE -> this.makeCollectionGen();
                case MegaMenuView.COLLECTION -> this.makeCollectionGen();
                case MegaMenuView.COMPENDIUM -> this.makeCollectionGen();
                case MegaMenuView.STATS -> this.makeCollectionGen();
                case MegaMenuView.POWERUPS -> this.makeCollectionGen();
                case MegaMenuView.QUESTS -> this.makeCollectionGen();
                case MegaMenuView.BADGES -> this.makeCollectionGen();
            };

            this.currentImage = imageGen.generate();
            this.sendResponse();
        } catch (SQLException exception) {
            log.error("Failed to get data.", exception);
        } catch (Exception exception) {
            log.error("Failed to generate collection image.", exception);
        }
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().complete();

        try {
            String pageInput = modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
            this.prevPage = this.page;
            this.page = Integer.parseInt(pageInput)-1;
            this.execute(null);
        } catch (NumberFormatException ignore) {}
    }

    private MegaMenuGenerator makeCollectionGen() throws SQLException {
        MegaMenuView view = MegaMenuView.COLLECTION;

        if (this.viewsToUpdateData.get(view) == null || !this.viewsToUpdateData.get(view)) {
            try (Connection connection = DataUtil.getConnection()) {
                this.boarInfos = this.boarUser.getBoarInfo(connection);
            }
        }

        int maxPage = Math.max((this.boarInfos.size()-1) / 15, 0);
        if (this.page > maxPage) {
            this.page = maxPage;
        }

        return new CollectionImageGenerator(
            this.page, this.boarUser, this.badgeIDs, this.firstJoinedDate, this.boarInfos
        );
    }

    private void sendResponse() {
        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(this.currentImage)
            .setComponents(this.getCurComponents());

        if (this.isStopped) {
            return;
        }

        this.interaction.getHook().editOriginal(editedMsg.build()).complete();
    }

    @Override
    public void stop(StopType type) throws IOException, InterruptedException {
        super.stop(type);
        this.boarUser.decRefs();
    }

    @Override
    public ActionRow[] getCurComponents() {
        this.curComponents = switch (this.curView) {
            case MegaMenuView.PROFILE -> getCollectionComponents();
            case MegaMenuView.COLLECTION -> getCollectionComponents();
            case MegaMenuView.COMPENDIUM -> getCollectionComponents();
            case MegaMenuView.STATS -> getCollectionComponents();
            case MegaMenuView.POWERUPS -> getCollectionComponents();
            case MegaMenuView.QUESTS -> getCollectionComponents();
            case MegaMenuView.BADGES -> getCollectionComponents();
        };

        return this.curComponents;
    }

    private ActionRow[] getCollectionComponents() {
        ActionRow[] nav = this.getNav();

        List<ItemComponent> boarFindBtn = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("boarFindBtn")
        );

        Button leftBtn = ((Button) nav[1].getComponents().getFirst()).asDisabled();
        Button pageBtn = ((Button) nav[1].getComponents().get(1)).asDisabled();
        Button rightBtn = ((Button) nav[1].getComponents().get(2)).asDisabled();

        int maxPage = Math.max((this.boarInfos.size()-1) / 15, 0);

        if (this.page > 0) {
            leftBtn = leftBtn.withDisabled(false);
        }

        if (maxPage > 0) {
            pageBtn = pageBtn.withDisabled(false);
        }

        if (this.page < maxPage) {
            rightBtn = rightBtn.withDisabled(false);
        }

        nav[1].getComponents().set(0, leftBtn);
        nav[1].getComponents().set(1, pageBtn);
        nav[1].getComponents().set(2, rightBtn);

        return new ActionRow[] {
            nav[0], nav[1], ActionRow.of(boarFindBtn)
        };
    }

    private ActionRow[] getNav() {
        List<ItemComponent> viewSelect = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("viewSelect")
        );
        List<ItemComponent> navBtns = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("leftBtn"),
            this.COMPONENTS.get("pageBtn"),
            this.COMPONENTS.get("rightBtn"),
            this.COMPONENTS.get("refreshBtn")
        );

        for (int i=0; i<this.navOptions.size(); i++) {
            SelectOption navOption = this.navOptions.get(i);

            if (navOption.getValue().equals(this.curView.toString())) {
                this.navOptions.set(i, navOption.withDefault(true));
                continue;
            }

            this.navOptions.set(i, navOption.withDefault(false));
        }

        StringSelectMenu viewSelectMenu = (StringSelectMenu) viewSelect.getFirst();
        viewSelect.set(0, new StringSelectMenuImpl(
            viewSelectMenu.getId(),
            viewSelectMenu.getPlaceholder(),
            viewSelectMenu.getMinValues(),
            viewSelectMenu.getMaxValues(),
            viewSelectMenu.isDisabled(),
            this.navOptions
        ));

        return new ActionRow[] {
            ActionRow.of(viewSelect), ActionRow.of(navBtns)
        };
    }

    private void makeSelectOptions(List<SelectOptionConfig> options) {
        List<SelectOption> navOptions = new ArrayList<>();

        for (SelectOptionConfig option : options) {
            SelectOption navOption = SelectOption.of(option.getLabel(), option.getValue())
                .withEmoji(InteractiveUtil.parseEmoji(option.getEmoji()))
                .withDescription(option.getDescription());

            navOptions.add(navOption);
        }

        this.navOptions = navOptions;
    }
}
