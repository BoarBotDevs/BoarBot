package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.entities.boaruser.*;
import dev.boarbot.entities.boaruser.data.*;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.interactives.gift.BoarGiftInteractive;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MegaMenuInteractive extends ModalInteractive implements Synchronizable {
    @Getter @Setter private int prevPage = -1;
    @Getter @Setter private int page;
    @Getter @Setter private int maxPage;
    @Getter @Setter private String boarPage;

    @Getter @Setter private MegaMenuView curView;
    @Getter @Setter private MegaMenuView prevView;
    @Getter private final Map<MegaMenuView, Boolean> viewsToUpdateData = new HashMap<>();

    @Getter @Setter private GenericComponentInteractionCreateEvent compEvent;
    @Getter @Setter private ModalInteractionEvent modalEvent;

    private final MegaMenuComponentsGetter componentsGetter = new MegaMenuComponentsGetter(this);
    private final MegaMenuGeneratorMaker generatorMaker = new MegaMenuGeneratorMaker(this);
    private ImageGenerator currentImageGen;
    private FileUpload currentImageUpload;

    @Getter private boolean isSkyblockGuild;

    @Getter @Setter private boolean confirmOpen = false;
    @Getter @Setter private String confirmString;

    @Getter @Setter private boolean acknowledgeOpen = false;
    @Getter @Setter private OverlayImageGenerator acknowledgeImageGen;

    @Getter @Setter private boolean filterOpen = false;
    @Getter @Setter private int filterBits = 0;

    @Getter @Setter private boolean sortOpen = false;
    @Getter @Setter private SortType sortVal = SortType.RARITY_D;

    @Getter @Setter private boolean interactOpen = false;
    @Getter @Setter private InteractType interactType;

    @Getter @Setter private String powerupUsing;

    @Getter private final BoarUser boarUser;

    @Getter private String firstJoinedDate;
    @Getter private List<BadgeData> badges;
    @Getter @Setter private String favoriteID;

    @Getter @Setter private Map<String, BoarInfo> ownedBoars;
    @Getter @Setter private Map<String, BoarInfo> filteredBoars;
    @Getter @Setter private Map.Entry<String, BoarInfo> curBoarEntry;
    @Getter @Setter private String curRarityKey;
    @Getter @Setter private int numTransmute;
    @Getter @Setter private int numClone;
    @Getter @Setter private int numTryClone;

    @Getter @Setter private ProfileData profileData;

    @Getter @Setter private StatsData statsData;

    @Getter @Setter private PowerupsData powData;
    @Getter @Setter private int numTryCharm;

    @Getter @Setter private QuestData questData;
    @Getter @Setter private List<String> questIDs;

    public MegaMenuInteractive(SlashCommandInteractionEvent event, MegaMenuView curView) throws SQLException {
        super(event.getInteraction());

        this.page = event.getOption("page") != null
            ? Math.max(Objects.requireNonNull(event.getOption("page")).getAsInt() - 1, 0)
            : 0;
        this.boarPage = event.getOption("search") != null
            ? Objects.requireNonNull(event.getOption("search")).getAsString()
            : null;
        this.boarUser = event.getOption("user") != null
            ? BoarUserFactory.getBoarUser(Objects.requireNonNull(event.getOption("user")).getAsUser())
            : BoarUserFactory.getBoarUser(event.getUser());

        this.curView = curView;
    }

    @Override
    public void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        this.attemptExecute(compEvent, null, startTime);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent != null) {
            if (!this.user.getId().equals(compEvent.getUser().getId())) {
                compEvent.deferEdit().queue();
                return;
            }

            if (this.modalHandler != null) {
                this.modalHandler.stop();
            }

            new MegaMenuComponentHandler(compEvent, this).handleCompEvent();
        }

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

            if (this.confirmOpen) {
                this.currentImageUpload = new OverlayImageGenerator(
                    this.currentImageGen.getImage(), this.confirmString
                ).generate().getFileUpload();
            } else if (this.acknowledgeOpen) {
                this.currentImageUpload = this.acknowledgeImageGen.setBaseImage(
                    this.currentImageGen.getImage()
                ).generate().getFileUpload();
            } else {
                this.currentImageUpload = this.currentImageGen.getFileUpload();
            }

            this.sendResponse();
        } catch (SQLException exception) {
            log.error("Failed to get data.", exception);
        } catch (Exception exception) {
            log.error("Failed to generate collection image.", exception);
        }
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        new MegaMenuComponentHandler(modalEvent, this).handleModalEvent();
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try {
            if (this.filterOpen) {
                try (Connection connection = DataUtil.getConnection()) {
                    boarUser.megaQuery().setFilterBits(connection, this.filterBits);
                }
            } else if (this.sortOpen) {
                try (Connection connection = DataUtil.getConnection()) {
                    boarUser.megaQuery().setSortVal(connection, this.sortVal);
                }
            } else if (this.interactType != null) {
                switch (this.interactType) {
                    case FAVORITE -> this.doFavorite(boarUser);
                    case CLONE -> this.doClone(boarUser);
                    case TRANSMUTE -> this.doTransmute(boarUser);
                }

                this.acknowledgeOpen = true;
                this.interactType = null;
                this.confirmOpen = false;
            } else if (this.powerupUsing != null) {
                switch (this.powerupUsing) {
                    case "miracle" -> this.doCharm(boarUser);
                    case "gift" -> this.doGift(boarUser);
                }

                this.acknowledgeOpen = true;
                this.powerupUsing = null;
                this.confirmOpen = false;
            }
        } catch (SQLException exception) {
            log.error("Failed to get user data", exception);
        }
    }

    private void doFavorite(BoarUser boarUser) throws SQLException {
        String boarName = BOARS.get(this.curBoarEntry.getKey()).getName();

        try (Connection connection = DataUtil.getConnection()) {
            this.favoriteID = boarUser.megaQuery().getFavoriteID(connection);

            if (this.favoriteID == null || !this.favoriteID.equals(this.curBoarEntry.getKey())) {
                this.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getCompFavoriteSuccess().formatted("<>" + this.curRarityKey + "<>" + boarName)
                );
                boarUser.megaQuery().setFavoriteID(connection, this.curBoarEntry.getKey());
            } else {
                this.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getCompUnfavoriteSuccess().formatted("<>" + this.curRarityKey + "<>" + boarName)
                );
                boarUser.megaQuery().setFavoriteID(connection, null);
            }
        }
    }

    private void doClone(BoarUser boarUser) throws SQLException {
        String boarName = BOARS.get(this.curBoarEntry.getKey()).getName();

        List<String> newBoarIDs = new ArrayList<>();
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();

        try (Connection connection = DataUtil.getConnection()) {
            this.numClone = boarUser.powQuery().getPowerupAmount(connection, "clone");
            boolean cloneable = RARITIES.get(this.curRarityKey).getAvgClones() != 0 &&
                this.numTryClone <= this.numClone;

            if (cloneable) {
                if (boarUser.boarQuery().hasBoar(this.curBoarEntry.getKey(), connection)) {
                    int avgClones = RARITIES.get(this.curRarityKey).getAvgClones();
                    double chance = this.numTryClone == avgClones
                        ? 1
                        : (double) (this.numTryClone % avgClones) / avgClones;
                    double randVal = Math.random();

                    for (int i = 0; i < (this.numTryClone / avgClones); i++) {
                        newBoarIDs.add(this.curBoarEntry.getKey());
                    }

                    if (chance < 1 && chance > randVal) {
                        newBoarIDs.add(this.curBoarEntry.getKey());
                    }

                    if (newBoarIDs.isEmpty()) {
                        this.acknowledgeImageGen = new OverlayImageGenerator(
                            null, STRS.getCompCloneFailed().formatted("<>" + this.curRarityKey + "<>" + boarName)
                        );
                    } else {
                        boarUser.boarQuery().addBoars(
                            newBoarIDs,
                            connection,
                            BoarObtainType.CLONE,
                            bucksGotten,
                            editions
                        );

                        boarUser.powQuery().usePowerup(connection, "clone", this.numTryClone);
                    }
                } else {
                    this.acknowledgeImageGen = new OverlayImageGenerator(
                        null, STRS.getCompNoBoar().formatted("<>" + this.curRarityKey + "<>" + boarName)
                    );
                }
            } else {
                this.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getNoPow().formatted(POWS.get("transmute").getPluralName())
                );
            }
        }

        if (!newBoarIDs.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                this.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getCompCloneSuccess().formatted("<>" + this.curRarityKey + "<>" + boarName)
                );

                String title = STRS.getCompCloneTitle();

                ItemInteractive.sendInteractive(
                    newBoarIDs, bucksGotten, editions, null, this.user, title, this.compEvent.getHook(), true
                );
            });
        }
    }

    private void doTransmute(BoarUser boarUser) throws SQLException {
        String boarName = BOARS.get(this.curBoarEntry.getKey()).getName();

        List<String> newBoarIDs = new ArrayList<>();
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();

        try (Connection connection = DataUtil.getConnection()) {
            this.numTransmute = boarUser.powQuery().getPowerupAmount(connection, "transmute");
            RarityConfig curRarity = RARITIES.get(this.curRarityKey);
            boolean transmutable = curRarity.getChargesNeeded() != 0 &&
                curRarity.getChargesNeeded() <= this.numTransmute;

            if (transmutable) {
                if (boarUser.boarQuery().hasBoar(this.curBoarEntry.getKey(), connection)) {
                    String nextRarityID = BoarUtil.getNextRarityKey(this.curRarityKey);
                    newBoarIDs.add(BoarUtil.findValid(nextRarityID, this.isSkyblockGuild));

                    boarUser.boarQuery().removeBoar(this.curBoarEntry.getKey(), connection);
                    boarUser.boarQuery().addBoars(
                        newBoarIDs,
                        connection,
                        BoarObtainType.TRANSMUTE,
                        bucksGotten,
                        editions
                    );

                    boarUser.powQuery().usePowerup(connection, "transmute", this.numTransmute);
                } else {
                    this.acknowledgeImageGen = new OverlayImageGenerator(
                        null, STRS.getCompNoBoar().formatted("<>" + this.curRarityKey + "<>" + boarName)
                    );
                }
            } else {
                this.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getNoPow().formatted(POWS.get("transmute").getPluralName())
                );
            }
        }

        if (!newBoarIDs.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                String newBoarName = BOARS.get(newBoarIDs.getFirst()).getName();
                String newBoarRarityKey = BoarUtil.findRarityKey(newBoarIDs.getFirst());

                this.boarPage = newBoarName;
                String overlayStr = STRS.getCompTransmuteSuccess().formatted(
                    "<>" + this.curRarityKey + "<>" + boarName,
                    "<>" + newBoarRarityKey + "<>" + newBoarName
                );

                this.acknowledgeImageGen = new OverlayImageGenerator(null, overlayStr);

                if (newBoarIDs.size() > 1) {
                    String firstBoarID = CONFIG.getMainConfig().getFirstBoarID();
                    String firstBoarName = BOARS.get(firstBoarID).getName();
                    String firstRarityKey = BoarUtil.findRarityKey(firstBoarID);

                    this.acknowledgeImageGen = new OverlayImageGenerator(
                        null, STRS.getCompTransmuteFirst().formatted("<>" + firstRarityKey + "<>" + firstBoarName)
                    );
                }

                String title = STRS.getCompTransmuteTitle();

                ItemInteractive.sendInteractive(
                    newBoarIDs, bucksGotten, editions, null, this.user, title, this.compEvent.getHook(), true
                );
            });
        }
    }

    private void doCharm(BoarUser boarUser) throws SQLException {
        try (Connection connection = DataUtil.getConnection()) {
            if (this.numTryCharm <= boarUser.powQuery().getPowerupAmount(connection, this.powerupUsing)) {
                boarUser.powQuery().activateMiracles(connection, this.numTryCharm);

                long blessings = this.boarUser.baseQuery().getBlessings(connection);
                this.acknowledgeImageGen = new OverlayImageGenerator(
                    null,
                    STRS.getPowMiracleSuccess().formatted(
                        STRS.getBlessingsPluralName(),
                        TextUtil.getBlessHex(blessings),
                        blessings > 1000
                            ? STRS.getBlessingsSymbol() + " "
                            : "",
                        blessings
                    )
                );

                return;
            }

            this.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getNoPow().formatted(POWS.get(this.powerupUsing).getPluralName())
            );
        }
    }

    private void doGift(BoarUser boarUser) throws SQLException {
        PowerupItemConfig powConfig = POWS.get(this.powerupUsing);

        try (Connection connection = DataUtil.getConnection()) {
            if (boarUser.powQuery().getPowerupAmount(connection, this.powerupUsing) > 0) {
                this.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getPowGiftSuccess().formatted(powConfig.getName())
                );

                Interactive giftInteractive = InteractiveFactory.constructInteractive(
                    this.compEvent, true, BoarGiftInteractive.class
                );
                giftInteractive.execute(null);

                return;
            }

            this.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getNoPow().formatted(powConfig.getPluralName())
            );
        }
    }

    private void sendResponse() {
        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(this.currentImageUpload)
            .setComponents(this.getCurComponents());

        if (this.isStopped) {
            return;
        }

        this.updateInteractive(editedMsg.build());
    }

    public int getFindBoarPage(String input) {
        int newPage = 0;
        boolean found = false;

        String cleanInput = input.replaceAll(" ", "").toLowerCase();
        Map<String, BoarInfo> filteredBoars = this.getFilteredBoars();

        // Find by search term first
        for (String boarID : filteredBoars.keySet()) {
            BoarItemConfig boar = BOARS.get(boarID);

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

        // Find by boar name (startsWith) second
        if (!found) {
            newPage = this.matchFront(cleanInput, filteredBoars);
            found = newPage <= this.maxPage;
        }

        // Find by shrinking boar name (startsWith) third
        if (!found) {
            cleanInput = cleanInput.substring(0, cleanInput.length()-1);

            while (!cleanInput.isEmpty()) {
                newPage = this.matchFront(cleanInput, filteredBoars);
                found = newPage <= this.getMaxPage();

                if (found) {
                    break;
                }

                cleanInput = cleanInput.substring(0, cleanInput.length()-1);
            }
        }

        if (!found) {
            newPage = this.page;
        }

        return newPage;
    }

    private int matchFront(String cleanInput, Map<String, BoarInfo> filteredBoars) {
        int newPage = 0;

        for (String boarID : filteredBoars.keySet()) {
            BoarItemConfig boar = BOARS.get(boarID);

            if (boar.getName().replaceAll(" ", "").toLowerCase().startsWith(cleanInput)) {
                break;
            }

            newPage++;
        }

        return newPage;
    }

    @Override
    public void stop(StopType type) throws IOException, InterruptedException {
        super.stop(type);
        this.boarUser.decRefs();
    }

    @Override
    public ActionRow[] getCurComponents() {
        return this.componentsGetter.getComponents();
    }
}
