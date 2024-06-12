package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.megamenu.CollectionImageGenerator;
import dev.boarbot.util.generators.megamenu.MegaMenuGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

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
import java.util.stream.Stream;

@Log4j2
public class MegaMenuInteractive extends Interactive {
    private int page = 0;
    private MegaMenuView curView;
    private ActionRow[] curComponents = new ActionRow[0];
    private List<SelectOption> navOptions = new ArrayList<>();

    private final BoarUser boarUser;

    private final Map<MegaMenuView, Boolean> viewsToUpdateData = new HashMap<>();
    private FileUpload currentImage;

    private final String firstJoinedDate;
    private List<String> badgeIDs;
    private Map<String, BoarInfo> boarInfos;

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getCollection();

    public MegaMenuInteractive(SlashCommandInteractionEvent initEvent, MegaMenuView curView) throws SQLException {
        super(initEvent);
        this.curView = curView;
        this.boarUser = BoarUserFactory.getBoarUser(initEvent.getUser());

        try (Connection connection = DataUtil.getConnection()) {
            long firstJoinedTimestamp = boarUser.getFirstJoinedTimestamp(connection);
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("MMMM d, yyyy")
                .toFormatter();

            this.firstJoinedDate = Instant.ofEpochMilli(firstJoinedTimestamp)
                .atOffset(ZoneOffset.UTC)
                .format(formatter);
        }

        this.makeSelectOptions(this.COMPONENTS.get("viewSelect").getOptions());
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        compEvent.deferEdit().queue();

        if (!this.initEvent.getUser().getId().equals(compEvent.getUser().getId())) {
            return;
        }

        String compID = compEvent.getComponentId().split(",")[1];

        // TODO: Prevent page increases if page is no longer available due to data change
        switch (compID) {
            case "LEFT" -> this.page++;
            case "RIGHT" -> this.page--;
        }

        try {
            try (Connection connection = DataUtil.getConnection()) {
                boolean shouldUpdateData = this.currentImage == null ||
                    this.lastEndTime <= this.boarUser.getLastChanged(connection);

                if (shouldUpdateData) {
                    this.badgeIDs = boarUser.getCurrentBadges(connection);
                    this.viewsToUpdateData.replaceAll((k, v) -> true);
                }

                // TODO: Send current image if nothing has changed
            }

            MegaMenuGenerator imageGen = switch (this.curView) {
                case MegaMenuView.PROFILE -> null;
                case MegaMenuView.COLLECTION -> this.makeCollectionGen();
                case MegaMenuView.COMPENDIUM -> null;
                case MegaMenuView.STATS -> null;
                case MegaMenuView.POWERUPS -> null;
                case MegaMenuView.QUESTS -> null;
                case MegaMenuView.BADGES -> null;
            };

            this.currentImage = imageGen.generate();
            this.sendResponse();
        } catch (SQLException exception) {
            log.error("Failed to get data.", exception);
        } catch (Exception exception) {
            log.error("Failed to generate collection image.", exception);
        }
    }

    private MegaMenuGenerator makeCollectionGen() throws SQLException {
        MegaMenuView view = MegaMenuView.COLLECTION;

        if (this.viewsToUpdateData.get(view) == null || !this.viewsToUpdateData.get(view)) {
            try (Connection connection = DataUtil.getConnection()) {
                this.boarInfos = this.boarUser.getBoarInfo(connection);
            }
        }

        return new CollectionImageGenerator(
            this.page, this.boarUser, this.badgeIDs, this.firstJoinedDate, this.boarInfos
        );
    }

    private void sendResponse() {
        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(this.currentImage)
            .setComponents(this.getCurComponents());

        this.interaction.getHook().editOriginal(editedMsg.build()).complete();
    }

    @Override
    public void stop(StopType type) throws IOException, InterruptedException {
        super.stop(type);
        this.boarUser.decRefs();
    }

    @Override
    public ActionRow[] getCurComponents() {
        ActionRow[] nav = this.getNav();

        ActionRow[] components = switch (this.curView) {
            case MegaMenuView.PROFILE -> getCollectionComponents();
            case MegaMenuView.COLLECTION -> getCollectionComponents();
            case MegaMenuView.COMPENDIUM -> getCollectionComponents();
            case MegaMenuView.STATS -> getCollectionComponents();
            case MegaMenuView.POWERUPS -> getCollectionComponents();
            case MegaMenuView.QUESTS -> getCollectionComponents();
            case MegaMenuView.BADGES -> getCollectionComponents();
        };

        this.curComponents = Stream.of(nav, components).flatMap(Stream::of).toArray(ActionRow[]::new);

        return this.curComponents;
    }

    private ActionRow[] getCollectionComponents() {
        List<ItemComponent> boarFindBtn = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("boarFindBtn")
        );

        return new ActionRow[] {
            ActionRow.of(boarFindBtn)
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
            ActionRow.of(viewSelect),
            ActionRow.of(navBtns)
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
