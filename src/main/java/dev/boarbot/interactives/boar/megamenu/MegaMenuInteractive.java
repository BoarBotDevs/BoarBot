package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.entities.boaruser.*;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.interactive.megamenu.*;
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
public class MegaMenuInteractive extends ModalInteractive implements Synchronizable {
    @Getter @Setter private int page;
    @Getter @Setter private int maxPage;
    @Getter @Setter private MegaMenuView curView;
    @Getter private boolean isSkyblockGuild;
    private final MegaMenuComponentsGetter componentsGetter = new MegaMenuComponentsGetter(this);
    @Getter @Setter private boolean filterOpen = false;
    @Getter @Setter private boolean sortOpen = false;
    @Getter @Setter private boolean interactOpen = false;
    @Getter @Setter private boolean confirmOpen = false;

    @Getter private final BoarUser boarUser;

    @Getter private final Map<MegaMenuView, Boolean> viewsToUpdateData = new HashMap<>();
    @Getter @Setter private int prevPage = -1;
    @Getter @Setter private MegaMenuView prevView;
    private final MegaMenuGeneratorMaker generatorMaker = new MegaMenuGeneratorMaker(this);
    private ImageGenerator currentImageGen;
    private FileUpload currentImageUpload;

    @Getter private String firstJoinedDate;
    @Getter private List<String> badgeIDs;
    @Getter private String favoriteID;
    @Getter @Setter private Map<String, BoarInfo> ownedBoars;
    @Getter @Setter private Map<String, BoarInfo> filteredBoars;
    @Getter @Setter private InteractType interactType;
    @Getter @Setter private int filterBits;
    @Getter @Setter private SortType sortVal;
    @Getter @Setter private Map.Entry<String, BoarInfo> curBoarEntry;
    @Getter @Setter private ProfileData profileData;

    public MegaMenuInteractive(SlashCommandInteractionEvent initEvent, MegaMenuView curView) throws SQLException {
        super(initEvent);

        this.page = initEvent.getOption("page") != null
            ? Math.max(initEvent.getOption("page").getAsInt() - 1, 0)
            : 0;
        this.boarUser = initEvent.getOption("user") != null
            ? BoarUserFactory.getBoarUser(initEvent.getOption("user").getAsUser())
            : BoarUserFactory.getBoarUser(initEvent.getUser());

        this.curView = curView;
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

                    this.isSkyblockGuild = GuildDataUtil.isSkyblockGuild(
                        connection, this.interaction.getGuild().getId()
                    );
                    this.badgeIDs = this.boarUser.getCurrentBadges(connection);
                    this.favoriteID = this.boarUser.getFavoriteID(connection);
                    this.viewsToUpdateData.replaceAll((k, v) -> false);
                }

                if (this.prevPage == this.page && this.prevView == this.curView && !this.confirmOpen) {
                    return;
                }
            }

            this.currentImageGen = this.generatorMaker.make().generate();

            if (this.confirmOpen) {
                this.currentImageUpload = new OverlayImageGenerator(this.currentImageGen.getImage(), "Hello")
                    .generate().getFileUpload();
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
                    case FAVORITE -> {
                        if (this.favoriteID == null || !this.favoriteID.equals(this.curBoarEntry.getKey())) {
                            boarUser.setFavoriteID(connection, this.curBoarEntry.getKey());
                        } else {
                            boarUser.setFavoriteID(connection, null);
                        }
                    }
                    case CLONE -> {}
                    case TRANSMUTE -> {}
                }
            }
        } catch (SQLException exception) {
            log.error("Failed to get update user's filter.", exception);
        }

        this.interactType = null;
    }

    private void sendResponse() {
        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(this.currentImageUpload)
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
        return this.componentsGetter.getComponents();
    }
}
