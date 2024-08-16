package dev.boarbot.interactives.boar.megamenu;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.bot.config.items.ItemConfig;
import dev.boarbot.entities.boaruser.*;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.python.PythonUtil;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
public class MegaMenuInteractive extends ModalInteractive implements Synchronizable {
    @Getter @Setter private int page;
    @Getter @Setter private int maxPage;
    @Getter @Setter private String boarPage;
    @Getter @Setter private MegaMenuView curView;
    @Getter private boolean isSkyblockGuild;
    private final MegaMenuComponentsGetter componentsGetter = new MegaMenuComponentsGetter(this);
    @Getter @Setter private boolean filterOpen = false;
    @Getter @Setter private boolean sortOpen = false;
    @Getter @Setter private boolean interactOpen = false;
    @Getter @Setter private boolean confirmOpen = false;
    @Getter @Setter private String confirmString;
    @Getter @Setter private boolean acknowledgeOpen = false;
    @Getter @Setter private String acknowledgeString;
    @Getter @Setter private boolean animated = false;

    @Getter @Setter private GenericComponentInteractionCreateEvent compEvent;
    @Getter @Setter private ModalInteractionEvent modalEvent;

    @Getter private final BoarUser boarUser;

    @Getter private final Map<MegaMenuView, Boolean> viewsToUpdateData = new HashMap<>();
    @Getter @Setter private int prevPage = -1;
    @Getter @Setter private MegaMenuView prevView;
    private final MegaMenuGeneratorMaker generatorMaker = new MegaMenuGeneratorMaker(this);
    private ImageGenerator currentImageGen;
    private FileUpload currentImageUpload;

    @Getter private String firstJoinedDate;
    @Getter private List<String> badgeIDs;
    @Getter @Setter private String favoriteID;
    @Getter @Setter private Map<String, BoarInfo> ownedBoars;
    @Getter @Setter private Map<String, BoarInfo> filteredBoars;
    @Getter @Setter private InteractType interactType;
    @Getter @Setter private int filterBits;
    @Getter @Setter private SortType sortVal;
    @Getter @Setter private Map.Entry<String, BoarInfo> curBoarEntry;
    @Getter @Setter private String curRarityKey;
    @Getter @Setter private ProfileData profileData;
    @Getter @Setter private int numTransmute;
    @Getter @Setter private int numClone;
    @Getter @Setter private int numTryClone;

    public MegaMenuInteractive(SlashCommandInteractionEvent event, MegaMenuView curView) throws SQLException {
        super(event.getInteraction());

        this.page = event.getOption("page") != null
            ? Math.max(event.getOption("page").getAsInt() - 1, 0)
            : 0;
        this.boarUser = event.getOption("user") != null
            ? BoarUserFactory.getBoarUser(event.getOption("user").getAsUser())
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
                    this.lastEndTime <= this.boarUser.getLastChanged(connection);

                if (shouldUpdateData) {
                    long firstJoinedTimestamp = this.boarUser.getFirstJoinedTimestamp(connection);

                    this.firstJoinedDate = this.boarUser.getFirstJoinedTimestamp(connection) > 0
                        ? Instant.ofEpochMilli(firstJoinedTimestamp)
                            .atOffset(ZoneOffset.UTC)
                            .format(TimeUtil.getDateFormatter())
                        : this.config.getStringConfig().getUnavailable();

                    this.isSkyblockGuild = GuildDataUtil.isSkyblockGuild(connection, this.guildID);
                    this.badgeIDs = this.boarUser.getCurrentBadges(connection);
                    this.viewsToUpdateData.replaceAll((k, v) -> false);
                }

                if (this.prevPage == this.page && this.prevView == this.curView && !this.confirmOpen) {
                    return;
                }
            }

            this.currentImageGen = this.generatorMaker.make().generate();

            boolean isAnimatedBoar = this.config.getItemConfig().getBoars().get(
                this.curBoarEntry.getKey()
            ).getStaticFile() != null;

            if (this.confirmOpen) {
                this.currentImageUpload = new OverlayImageGenerator(
                    this.currentImageGen.getImage(), this.confirmString
                ).generate().getFileUpload();
            } else if (this.acknowledgeOpen) {
                this.currentImageUpload = new OverlayImageGenerator(
                    this.currentImageGen.getImage(), this.acknowledgeString
                ).generate().getFileUpload();
            } else if (this.animated && isAnimatedBoar) {
                this.animated = false;

                Gson g = new Gson();
                String filePath = this.config.getPathConfig().getBoars() +
                    this.config.getItemConfig().getBoars().get(this.curBoarEntry.getKey()).getFile();

                BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get(
                    "borderMediumBig" + this.curRarityKey
                );

                ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                ImageIO.write(rarityBorderImage, "png", byteArrayOS);
                byte[] rarityBorderBytes = byteArrayOS.toByteArray();

                Process pythonProcess = new ProcessBuilder(
                    "python",
                    this.config.getPathConfig().getApplyScript(),
                    g.toJson(this.config.getNumberConfig()),
                    filePath,
                    Integer.toString(this.currentImageGen.getBytes().length),
                    Integer.toString(rarityBorderBytes.length)
                ).start();

                this.currentImageUpload = FileUpload.fromData(
                    PythonUtil.getResult(pythonProcess, this.currentImageGen.getBytes(), rarityBorderBytes),
                    "unknown.gif"
                );
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
        try (Connection connection = DataUtil.getConnection()) {
            if (this.filterOpen) {
                boarUser.setFilterBits(connection, this.filterBits);
            } else if (this.sortOpen) {
                boarUser.setSortVal(connection, this.sortVal);
            } else if (this.interactOpen) {
                switch (this.interactType) {
                    case FAVORITE -> this.doFavorite(connection, boarUser);
                    case CLONE -> this.doClone(connection, boarUser);
                    case TRANSMUTE -> this.doTransmute(connection, boarUser);
                }
                this.acknowledgeOpen = true;
            }
        } catch (SQLException exception) {
            log.error("Failed to get user data", exception);
        } finally {
            this.confirmOpen = false;
            this.interactType = null;
        }
    }

    private void doFavorite(Connection connection, BoarUser boarUser) throws SQLException {
        StringConfig strConfig = this.config.getStringConfig();
        String boarName = this.config.getItemConfig().getBoars().get(this.curBoarEntry.getKey()).getName();

        this.favoriteID = boarUser.getFavoriteID(connection);

        if (this.favoriteID == null || !this.favoriteID.equals(this.curBoarEntry.getKey())) {
            this.acknowledgeString = strConfig.getCompFavoriteSuccess().formatted(
                "<>" + this.curRarityKey + "<>" + boarName
            );
            boarUser.setFavoriteID(connection, this.curBoarEntry.getKey());
        } else {
            this.acknowledgeString = strConfig.getCompUnfavoriteSuccess().formatted(
                "<>" + this.curRarityKey + "<>" + boarName
            );
            boarUser.setFavoriteID(connection, null);
        }
    }

    private void doClone(Connection connection, BoarUser boarUser) throws SQLException {
        StringConfig strConfig = this.config.getStringConfig();
        ItemConfig itemConfig = this.config.getItemConfig();
        Map<String, IndivItemConfig> powConfig = itemConfig.getPowerups();
        String boarName = itemConfig.getBoars().get(this.curBoarEntry.getKey()).getName();

        this.numClone = boarUser.getPowerupAmount(connection, "clone");
        boolean cloneable = this.config.getRarityConfigs().get(this.curRarityKey).getAvgClones() != -1 &&
            this.numTryClone <= this.numClone;

        if (cloneable) {
            if (boarUser.hasBoar(this.curBoarEntry.getKey(), connection)) {
                int avgClones = this.config.getRarityConfigs().get(this.curRarityKey).getAvgClones();
                double chance = this.numTryClone == avgClones
                    ? 1
                    : (double) (this.numTryClone % avgClones) / avgClones;
                double randVal = Math.random();

                List<String> newBoarIDs = new ArrayList<>();

                for (int i=0; i<(this.numTryClone / avgClones); i++) {
                    newBoarIDs.add(this.curBoarEntry.getKey());
                }

                if (chance > randVal) {
                    newBoarIDs.add(this.curBoarEntry.getKey());
                }

                if (newBoarIDs.isEmpty()) {
                    this.acknowledgeString = strConfig.getCompCloneFailed().formatted(boarName);
                } else {
                    List<Integer> bucksGotten = new ArrayList<>();
                    List<Integer> editions = new ArrayList<>();

                    boarUser.addBoars(
                        newBoarIDs,
                        connection,
                        BoarObtainType.CLONE,
                        bucksGotten,
                        editions
                    );

                    boarUser.usePowerup(connection, "clone", this.numTryClone);

                    this.acknowledgeString = strConfig.getCompCloneSuccess().formatted(boarName);

                    String title = this.config.getStringConfig().getCompCloneTitle();

                    ItemInteractive.sendInteractive(
                        newBoarIDs, bucksGotten, editions, this.user, title, this.compEvent.getHook(), true
                    );
                }
            } else {
                this.acknowledgeString = strConfig.getCompNoBoar().formatted(
                    "<>" + this.curRarityKey + "<>" + boarName
                );
            }
        } else {
            this.acknowledgeString = strConfig.getCompNoPow().formatted(
                powConfig.get("transmute").getPluralName()
            );
        }
    }

    private void doTransmute(Connection connection, BoarUser boarUser) throws SQLException {
        StringConfig strConfig = this.config.getStringConfig();
        ItemConfig itemConfig = this.config.getItemConfig();
        Map<String, IndivItemConfig> powConfig = itemConfig.getPowerups();
        String boarName = itemConfig.getBoars().get(this.curBoarEntry.getKey()).getName();

        this.numTransmute = boarUser.getPowerupAmount(connection, "transmute");
        RarityConfig curRarity = this.config.getRarityConfigs().get(this.curRarityKey);
        boolean transmutable = curRarity.getChargesNeeded() != -1 &&
            curRarity.getChargesNeeded() <= this.numTransmute;

        if (transmutable) {
            if (boarUser.hasBoar(this.curBoarEntry.getKey(), connection)) {
                List<String> newBoarIDs = new ArrayList<>();
                String nextRarityID = BoarUtil.getNextRarityKey(this.curRarityKey);

                newBoarIDs.add(BoarUtil.findValid(nextRarityID, this.isSkyblockGuild));

                String newBoarName = itemConfig.getBoars().get(newBoarIDs.getFirst()).getName();
                String newBoarRarityKey = BoarUtil.findRarityKey(newBoarIDs.getFirst());

                List<Integer> bucksGotten = new ArrayList<>();
                List<Integer> editions = new ArrayList<>();

                boarUser.removeBoar(this.curBoarEntry.getKey(), connection);
                boarUser.addBoars(
                    newBoarIDs,
                    connection,
                    BoarObtainType.TRANSMUTE,
                    bucksGotten,
                    editions
                );

                this.boarPage = newBoarName;
                boarUser.usePowerup(connection, "transmute", this.numTransmute);

                this.acknowledgeString = strConfig.getCompTransmuteSuccess().formatted(
                    "<>" + this.curRarityKey + "<>" + boarName,
                    "<>" + newBoarRarityKey + "<>" + newBoarName
                );

                if (newBoarIDs.size() > 1) {
                    String firstBoarID = this.config.getMainConfig().getFirstBoarID();
                    String firstBoarName = itemConfig.getBoars().get(firstBoarID).getName();
                    String firstRarityKey = BoarUtil.findRarityKey(firstBoarID);

                    this.acknowledgeString += strConfig.getCompTransmuteFirst().formatted(
                        "<>" + firstRarityKey + "<>" + firstBoarName
                    );
                }

                String title = this.config.getStringConfig().getCompTransmuteTitle();

                ItemInteractive.sendInteractive(
                    newBoarIDs, bucksGotten, editions, this.user, title, this.compEvent.getHook(), true
                );
            } else {
                this.acknowledgeString = strConfig.getCompNoBoar().formatted(
                    "<>" + this.curRarityKey + "<>" + boarName
                );
            }
        } else {
            this.acknowledgeString = strConfig.getCompNoPow().formatted(
                powConfig.get("transmute").getPluralName()
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

        // Find by boar name (startsWith) second
        if (!found) {
            newPage = this.matchFront(cleanInput, filteredBoars);
            found = newPage <= this.getMaxPage();
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

        return newPage;
    }

    private int matchFront(String cleanInput, Map<String, BoarInfo> filteredBoars) {
        int newPage = 0;

        for (String boarID : filteredBoars.keySet()) {
            IndivItemConfig boar = this.config.getItemConfig().getBoars().get(boarID);

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
