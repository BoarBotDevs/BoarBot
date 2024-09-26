package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.entities.boaruser.*;
import dev.boarbot.entities.boaruser.data.*;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

public class MegaMenuInteractive extends ModalInteractive implements Synchronizable {
    int page;
    int maxPage;
    String boarPage;
    MegaMenuView curView;
    MegaMenuView prevView;
    @Getter private final Map<MegaMenuView, Boolean> viewsToUpdateData = new HashMap<>();

    GenericComponentInteractionCreateEvent compEvent;
    ModalInteractionEvent modalEvent;

    private final MegaMenuComponentsGetter componentsGetter = new MegaMenuComponentsGetter(this);
    private final MegaMenuGeneratorMaker generatorMaker = new MegaMenuGeneratorMaker(this);
    private final MegaMenuActionHandler actionHandler = new MegaMenuActionHandler(this);
    private ImageGenerator currentImageGen;
    private FileUpload currentImageUpload;

    boolean confirmOpen = false;
    String confirmString;
    boolean acknowledgeOpen = false;
    OverlayImageGenerator acknowledgeImageGen;

    @Getter private BoarUser boarUser;
    @Getter private boolean isSkyblockGuild;
    @Getter private String firstJoinedDate;
    @Getter private List<BadgeData> badges;
    String favoriteID;

    ProfileData profileData;

    Map<String, BoarInfo> ownedBoars;
    Map<String, BoarInfo> filteredBoars;
    Map.Entry<String, BoarInfo> curBoarEntry;
    String curRarityKey;
    boolean filterOpen = false;
    int filterBits = 0;
    boolean sortOpen = false;
    SortType sortVal = SortType.RARITY_D;
    boolean interactOpen = false;
    InteractType interactType;
    int numTransmute;
    int numClone;
    int numTryClone;

    StatsData statsData;

    PowerupsData powData;
    String powerupUsing;
    int numTryCharm;

    QuestData questData;
    List<QuestType> quests;
    QuestAction questAction;

    public MegaMenuInteractive(SlashCommandInteractionEvent event, MegaMenuView curView) {
        super(event.getInteraction());

        this.page = event.getOption("page") != null
            ? Math.max(Objects.requireNonNull(event.getOption("page")).getAsInt() - 1, 0)
            : 0;
        this.boarPage = event.getOption("search") != null
            ? Objects.requireNonNull(event.getOption("search")).getAsString()
            : null;

        try {
            this.boarUser = event.getOption("user") != null
                ? BoarUserFactory.getBoarUser(Objects.requireNonNull(event.getOption("user")).getAsUser())
                : BoarUserFactory.getBoarUser(event.getUser());
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update data", exception);
            return;
        }

        this.curView = curView;
    }

    @Override
    public void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        this.attemptExecute(compEvent, null, startTime);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent == null) {
            this.trySendResponse();
            return;
        }

        if (this.modalHandler != null) {
            this.modalHandler.stop();
        }

        new MegaMenuComponentHandler(compEvent, this).handleCompEvent();
        this.trySendResponse();
    }

    private void trySendResponse() {
        try {
            try (Connection connection = DataUtil.getConnection()) {
                boolean shouldUpdateData = this.currentImageGen == null ||
                    this.lastEndTime <= this.boarUser.baseQuery().getLastChanged(connection);

                if (shouldUpdateData) {
                    long firstJoinedTimestamp = this.boarUser.megaQuery().getFirstJoinedTimestamp(connection);

                    this.firstJoinedDate = this.boarUser.megaQuery().getFirstJoinedTimestamp(connection) > 0
                        ? Instant.ofEpochMilli(firstJoinedTimestamp)
                            .atOffset(ZoneOffset.UTC)
                            .format(TimeUtil.getDateFormatter())
                        : STRS.getUnavailable();
                    this.favoriteID = this.boarUser.megaQuery().getFavoriteID(connection);

                    this.isSkyblockGuild = GuildDataUtil.isSkyblockGuild(connection, this.guildID);
                    this.badges = this.boarUser.megaQuery().getCurrentBadges(connection);
                    this.viewsToUpdateData.replaceAll((k, v) -> false);
                }
            }

            this.currentImageGen = this.generatorMaker.make().generate();
            Log.debug(this.user, this.getClass(), "Successfully generated image for " + this.curView);

            if (this.confirmOpen) {
                this.currentImageUpload = new OverlayImageGenerator(
                    this.currentImageGen.getImage(), this.confirmString
                ).generate().getFileUpload();
                Log.debug(this.user, this.getClass(), "Confirmation screen active");
            } else if (this.acknowledgeOpen) {
                this.currentImageUpload = this.acknowledgeImageGen.setBaseImage(
                    this.currentImageGen.getImage()
                ).generate().getFileUpload();
                Log.debug(this.user, this.getClass(), "Acknowledge screen active");
            } else {
                this.currentImageUpload = this.currentImageGen.getFileUpload();
            }

            this.sendResponse();
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to get update data for " + this.curView, exception);
        } catch (IOException | URISyntaxException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate %s image".formatted(this.curView), exception);
        }
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        new MegaMenuComponentHandler(modalEvent, this).handleModalEvent();
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        this.actionHandler.doAction(boarUser);
    }

    private void sendResponse() {
        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(this.currentImageUpload).setComponents(this.getCurComponents());

        this.updateInteractive(false, editedMsg.build());
    }

    public int getFindBoarPage(String input) {
        int newPage = 0;

        String cleanInput = input.replaceAll(" ", "").toLowerCase();
        Map<String, BoarInfo> filteredBoars = this.filteredBoars;

        // Find by search term first
        for (String boarID : filteredBoars.keySet()) {
            BoarItemConfig boar = BOARS.get(boarID);

            for (String searchTerm : boar.getSearchTerms()) {
                if (cleanInput.equals(searchTerm)) {
                    Log.debug(this.user, this.getClass(), "Found boar: " + boarID);
                    return newPage;
                }
            }

            newPage++;
        }

        // Find by boar name (startsWith) second
        newPage = this.matchFront(cleanInput, filteredBoars);
        if (newPage <= this.maxPage) {
            return newPage;
        }

        // Find by shrinking boar name (startsWith) third
        cleanInput = cleanInput.substring(0, cleanInput.length()-1);
        while (!cleanInput.isEmpty()) {
            newPage = this.matchFront(cleanInput, filteredBoars);

            if (newPage <= this.maxPage) {
                return newPage;
            }

            cleanInput = cleanInput.substring(0, cleanInput.length()-1);
        }

        Log.debug(this.user, this.getClass(), "Failed to find boar");
        return this.page;
    }

    private int matchFront(String cleanInput, Map<String, BoarInfo> filteredBoars) {
        int newPage = 0;

        for (String boarID : filteredBoars.keySet()) {
            BoarItemConfig boar = BOARS.get(boarID);

            if (boar.getName().replaceAll(" ", "").toLowerCase().startsWith(cleanInput)) {
                Log.debug(this.user, this.getClass(), "Found boar: " + boarID);
                return newPage;
            }

            newPage++;
        }

        return newPage;
    }

    @Override
    public ActionRow[] getCurComponents() {
        return this.componentsGetter.getComponents();
    }
}
