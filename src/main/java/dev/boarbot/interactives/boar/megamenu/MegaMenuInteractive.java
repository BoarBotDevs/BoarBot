package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.ProfileData;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.FindBoarModalHandler;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.modals.PageInputModalHandler;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.megamenu.CollectionImageGenerator;
import dev.boarbot.util.generators.megamenu.CompendiumImageGenerator;
import dev.boarbot.util.generators.megamenu.MegaMenuGenerator;
import dev.boarbot.util.generators.megamenu.ProfileImageGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.modal.ModalUtil;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

@Slf4j
public class MegaMenuInteractive extends ModalInteractive {
    private int page;
    private int maxPage;
    private MegaMenuView curView;
    private boolean isSkyblockGuild;
    private ActionRow[] curComponents = new ActionRow[0];
    private boolean filterOpen = false;
    private boolean sortOpen = false;
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
    private Map<String, BoarInfo> ownedBoars;
    private Map<String, BoarInfo> filteredBoars;
    private String filterVal;
    private Map.Entry<String, BoarInfo> curBoarEntry;
    private ProfileData profileData;

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

            if (!compID.equals("PAGE") && !compID.equals("BOAR_FIND")) {
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

                    this.filterOpen = false;
                    this.sortOpen = false;
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

                case "BOAR_FIND" -> {
                    ModalConfig modalConfig = this.config.getModalConfig().get("findBoar");

                    Modal modal = new ModalImpl(
                        ModalUtil.makeModalID(modalConfig.getId(), compEvent),
                        modalConfig.getTitle(),
                        ModalUtil.makeModalComponents(modalConfig.getComponents())
                    );

                    this.modalHandler = new FindBoarModalHandler(compEvent, this);
                    compEvent.replyModal(modal).complete();
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

                    this.isSkyblockGuild = GuildDataUtil.isSkyblockGuild(
                        connection, this.interaction.getGuild().getId()
                    );
                    this.badgeIDs = this.boarUser.getCurrentBadges(connection);
                    this.viewsToUpdateData.replaceAll((k, v) -> false);
                }

                if (this.prevPage == this.page && this.prevView == this.curView) {
                    return;
                }
            }

            MegaMenuGenerator imageGen = switch (this.curView) {
                case MegaMenuView.PROFILE -> this.makeProfileGen();
                case MegaMenuView.COLLECTION -> this.makeCollectionGen();
                case MegaMenuView.COMPENDIUM -> this.makeCompendiumGen();
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

        switch (modalEvent.getModalId().split(",")[2]) {
            case "PAGE_INPUT" -> {
                try {
                    String pageInput = modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
                    this.prevPage = this.page;
                    this.page = Math.max(Integer.parseInt(pageInput)-1, 0);
                    this.execute(null);
                } catch (NumberFormatException ignore) {}
            }

            case "FIND_BOAR" -> {
                int newPage = 0;
                boolean found = false;

                String cleanInput = modalEvent.getValues().getFirst().getAsString().replaceAll(" ", "").toLowerCase();

                for (String boarID : this.filteredBoars.keySet()) {
                    IndivItemConfig boar = this.config.getItemConfig().getBoars().get(boarID);

                    for (String searchTerm : boar.getSearchTerms()) {
                        if (cleanInput.equals(searchTerm)) {
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        break;
                    }

                    newPage++;
                }

                if (!found) {
                    newPage = this.matchFront(cleanInput);
                    found = newPage <= this.maxPage;
                }

                if (!found) {
                    cleanInput = cleanInput.substring(0, cleanInput.length()-1);

                    while (!cleanInput.isEmpty()) {
                        newPage = matchFront(cleanInput);
                        found = newPage <= this.maxPage;

                        if (found) {
                            break;
                        }

                        cleanInput = cleanInput.substring(0, cleanInput.length()-1);
                    }
                }

                this.page = newPage;
                this.execute(null);
            }
        }
    }

    private int matchFront(String cleanInput) {
        int newPage = 0;

        for (String boarID : this.filteredBoars.keySet()) {
            IndivItemConfig boar = this.config.getItemConfig().getBoars().get(boarID);

            if (boar.getName().replaceAll(" ", "").toLowerCase().startsWith(cleanInput)) {
                break;
            }

            newPage++;
        }

        return newPage;
    }

    public MegaMenuGenerator makeProfileGen() throws SQLException {
        MegaMenuView view = MegaMenuView.PROFILE;

        if (this.viewsToUpdateData.get(view) == null || !this.viewsToUpdateData.get(view)) {
            try (Connection connection = DataUtil.getConnection()) {
                this.profileData = this.boarUser.getProfileData(connection);
                this.viewsToUpdateData.put(view, true);
            }
        }

        this.maxPage = 0;
        if (this.page > this.maxPage) {
            this.page = this.maxPage;
        }

        return new ProfileImageGenerator(
            this.page, this.boarUser, this.badgeIDs, this.firstJoinedDate, this.isSkyblockGuild, this.profileData
        );
    }

    private MegaMenuGenerator makeCollectionGen() throws SQLException {
        MegaMenuView view = MegaMenuView.COLLECTION;

        if (this.viewsToUpdateData.get(view) == null || !this.viewsToUpdateData.get(view)) {
            try (Connection connection = DataUtil.getConnection()) {
                this.ownedBoars = this.boarUser.getOwnedBoarInfo(connection);
                this.filterVal = this.boarUser.getFilterVal(connection);
                this.viewsToUpdateData.put(view, true);
                this.viewsToUpdateData.put(MegaMenuView.COMPENDIUM, true);
            }
        }

        this.refreshFilter();

        this.maxPage = Math.max((this.filteredBoars.size()-1) / 15, 0);
        if (this.page > this.maxPage) {
            this.page = this.maxPage;
        }

        return new CollectionImageGenerator(
            this.page, this.boarUser, this.badgeIDs, this.firstJoinedDate, this.filteredBoars
        );
    }

    private MegaMenuGenerator makeCompendiumGen() throws SQLException {
        MegaMenuView view = MegaMenuView.COMPENDIUM;

        if (this.viewsToUpdateData.get(view) == null || !this.viewsToUpdateData.get(view)) {
            try (Connection connection = DataUtil.getConnection()) {
                this.ownedBoars = this.boarUser.getOwnedBoarInfo(connection);
                this.filterVal = this.boarUser.getFilterVal(connection);
                this.viewsToUpdateData.put(view, true);
                this.viewsToUpdateData.put(MegaMenuView.COLLECTION, true);
            }
        }

        this.refreshFilter();

        this.maxPage = this.filteredBoars.size()-1;

        if (this.page > this.maxPage) {
            this.page = this.maxPage;
        }

        Iterator<Map.Entry<String, BoarInfo>> iterator = this.filteredBoars.entrySet().iterator();
        for (int i=0; i<this.page; i++) {
            iterator.next();
        }
        this.curBoarEntry = iterator.next();

        return new CompendiumImageGenerator(
            this.page,
            this.boarUser,
            this.badgeIDs,
            this.firstJoinedDate,
            this.curBoarEntry
        );
    }

    private void refreshFilter() {
        this.filteredBoars = new LinkedHashMap<>();
        List<Map.Entry<String, RarityConfig>> rarityEntries = this.config.getRarityConfigs()
            .entrySet()
            .stream()
            .toList()
            .reversed();

        String unavailable = this.config.getStringConfig().getUnavailable();

        for (Map.Entry<String, RarityConfig> rarityEntry : rarityEntries) {
            BoarInfo emptyBoarInfo = new BoarInfo(0, rarityEntry.getKey(), unavailable, unavailable);

            for (String boarID : rarityEntry.getValue().getBoars()) {
                // Owned filter
                if (this.filterVal != null && this.filterVal.equals("owned") && !this.ownedBoars.containsKey(boarID)) {
                    continue;
                }

                // Duplicate filter
                boolean hasDuplicate = this.ownedBoars.containsKey(boarID) && this.ownedBoars.get(boarID).amount() > 1;
                if (this.filterVal != null && this.filterVal.equals("duplicate") && !hasDuplicate) {
                    continue;
                }

                // No filter
                if (rarityEntry.getValue().isHidden() && !this.ownedBoars.containsKey(boarID)) {
                    continue;
                }

                this.filteredBoars.put(boarID, this.ownedBoars.getOrDefault(boarID, emptyBoarInfo));
            }
        }
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
            case MegaMenuView.PROFILE -> getProfileComponents();
            case MegaMenuView.COLLECTION -> getCollectionComponents();
            case MegaMenuView.COMPENDIUM -> getCompendiumComponents();
            case MegaMenuView.STATS -> getCollectionComponents();
            case MegaMenuView.POWERUPS -> getCollectionComponents();
            case MegaMenuView.QUESTS -> getCollectionComponents();
            case MegaMenuView.BADGES -> getCollectionComponents();
        };

        return this.curComponents;
    }

    private ActionRow[] getProfileComponents() {
        ActionRow[] nav = this.getNav();

        Button leftBtn = ((Button) nav[1].getComponents().getFirst()).asDisabled();
        Button pageBtn = ((Button) nav[1].getComponents().get(1)).asDisabled();
        Button rightBtn = ((Button) nav[1].getComponents().get(2)).asDisabled();

        nav[1].getComponents().set(0, leftBtn);
        nav[1].getComponents().set(1, pageBtn);
        nav[1].getComponents().set(2, rightBtn);

        return new ActionRow[] {
            nav[0], nav[1]
        };
    }

    private ActionRow[] getCollectionComponents() {
        ActionRow[] nav = this.getNav();

        List<ItemComponent> filterSortRow = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("filterBtn"),
            this.COMPONENTS.get("sortBtn")
        );

        Button leftBtn = ((Button) nav[1].getComponents().getFirst()).asDisabled();
        Button pageBtn = ((Button) nav[1].getComponents().get(1)).asDisabled();
        Button rightBtn = ((Button) nav[1].getComponents().get(2)).asDisabled();

        if (this.page > 0) {
            leftBtn = leftBtn.withDisabled(false);
        }

        if (this.maxPage > 0) {
            pageBtn = pageBtn.withDisabled(false);
        }

        if (this.page < this.maxPage) {
            rightBtn = rightBtn.withDisabled(false);
        }

        nav[1].getComponents().set(0, leftBtn);
        nav[1].getComponents().set(1, pageBtn);
        nav[1].getComponents().set(2, rightBtn);

        return new ActionRow[] {
            nav[0], nav[1], ActionRow.of(filterSortRow)
        };
    }

    private ActionRow[] getCompendiumComponents() {
        ActionRow[] nav = this.getNav();

        List<ItemComponent> interactRow = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("boarFindBtn"),
            this.COMPONENTS.get("interactBtn")
        );

        List<ItemComponent> filterSortRow = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("filterBtn"),
            this.COMPONENTS.get("sortBtn")
        );

        Button leftBtn = ((Button) nav[1].getComponents().getFirst()).asDisabled();
        Button pageBtn = ((Button) nav[1].getComponents().get(1)).asDisabled();
        Button rightBtn = ((Button) nav[1].getComponents().get(2)).asDisabled();
        Button interactBtn = ((Button) interactRow.get(1)).asDisabled();

        if (this.page > 0) {
            leftBtn = leftBtn.withDisabled(false);
        }

        if (this.maxPage > 0) {
            pageBtn = pageBtn.withDisabled(false);
        }

        if (this.page < this.maxPage) {
            rightBtn = rightBtn.withDisabled(false);
        }

        boolean userSelf = this.interaction.getUser().getId().equals(this.boarUser.getUserID());
        if (this.curBoarEntry.getValue().amount() > 0 && userSelf) {
            interactBtn = interactBtn.withDisabled(false);
        }

        nav[1].getComponents().set(0, leftBtn);
        nav[1].getComponents().set(1, pageBtn);
        nav[1].getComponents().set(2, rightBtn);
        interactRow.set(1, interactBtn);

        return new ActionRow[] {
            nav[0], nav[1], ActionRow.of(interactRow), ActionRow.of(filterSortRow)
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
