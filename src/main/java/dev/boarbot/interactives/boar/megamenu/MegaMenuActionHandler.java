package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.quests.QuestInfo;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.quests.QuestUtil;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

class MegaMenuActionHandler implements Configured {
    private final MegaMenuInteractive interactive;
    private final User user;

    private Map.Entry<String, BoarInfo> curBoarEntry;
    private String curRarityKey;

    public MegaMenuActionHandler(MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.user = interactive.getUser();
    }

    public void doAction(BoarUser boarUser) {
        if (this.interactive.filterOpen) {
            try (Connection connection = DataUtil.getConnection()) {
                boarUser.megaQuery().setFilterBits(connection, this.interactive.filterBits);
                Log.debug(boarUser.getUser(), this.getClass(), "Set filter bits to " + this.interactive.filterBits);
            } catch (SQLException exception) {
                this.interactive.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to update filter bits", exception);
            }
        } else if (this.interactive.sortOpen) {
            try (Connection connection = DataUtil.getConnection()) {
                boarUser.megaQuery().setSortVal(connection, this.interactive.sortVal);
                Log.debug(boarUser.getUser(), this.getClass(), "Set sort value to " + this.interactive.sortVal);
            } catch (SQLException exception) {
                this.interactive.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to update sort value", exception);
            }
        } else if (this.interactive.interactType != null) {
            this.curBoarEntry = this.interactive.curBoarEntry;
            this.curRarityKey = this.interactive.curRarityKey;

            try (Connection connection = DataUtil.getConnection()) {
                switch (this.interactive.interactType) {
                    case FAVORITE -> this.doFavorite(boarUser, connection);
                    case CLONE -> this.doClone(boarUser, connection);
                    case TRANSMUTE -> this.doTransmute(boarUser, connection);
                }
            } catch (SQLException exception) {
                this.interactive.stop(StopType.EXCEPTION);
                Log.error(
                    this.user,
                    this.getClass(),
                    "Failed to perform %s due to database issue".formatted(this.interactive.interactType),
                    exception
                );
            }

            this.interactive.acknowledgeOpen = true;
            this.interactive.interactType = null;
            this.interactive.confirmOpen = false;
        } else if (this.interactive.powerupUsing != null) {
            try (Connection connection = DataUtil.getConnection()) {
                switch (this.interactive.powerupUsing) {
                    case "miracle" -> this.doCharm(boarUser, connection);
                    case "gift" -> this.doGift(boarUser, connection);
                }
            } catch (SQLException exception) {
                this.interactive.stop(StopType.EXCEPTION);
                Log.error(
                    this.user,
                    this.getClass(),
                    "Failed to use %s powerup due to database issue".formatted(this.interactive.powerupUsing),
                    exception
                );
            }

            this.interactive.acknowledgeOpen = true;
            this.interactive.powerupUsing = null;
            this.interactive.confirmOpen = false;
        } else if (this.interactive.questAction != null) {
            try (Connection connection = DataUtil.getConnection()) {
                switch (this.interactive.questAction) {
                    case CLAIM -> this.doQuestClaim(boarUser, connection);
                    case CLAIM_BONUS -> this.doQuestBonus(boarUser, connection);
                    case AUTO_CLAIM -> this.toggleQuestAuto(boarUser, connection);
                }
            } catch (SQLException exception) {
                this.interactive.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to update quest data", exception);
            }

            this.interactive.questAction = null;
        }
    }

    public void doFavorite(BoarUser boarUser, Connection connection) throws SQLException {
        String boarName = BOARS.get(this.curBoarEntry.getKey()).getName();

        this.interactive.favoriteID = boarUser.megaQuery().getFavoriteID(connection);
        boolean doFavorite = this.interactive.favoriteID == null ||
            !this.interactive.favoriteID.equals(this.curBoarEntry.getKey());

        if (doFavorite) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getCompFavoriteSuccess().formatted("<>" + this.curRarityKey + "<>" + boarName)
            );
            boarUser.megaQuery().setFavoriteID(connection, this.curBoarEntry.getKey());
            Log.debug(boarUser.getUser(), this.getClass(), "Favorited " + this.curBoarEntry.getKey());
        } else {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getCompUnfavoriteSuccess().formatted("<>" + this.curRarityKey + "<>" + boarName)
            );
            boarUser.megaQuery().setFavoriteID(connection, null);
            Log.debug(boarUser.getUser(), this.getClass(), "Unfavorited " + this.curBoarEntry.getKey());
        }
    }

    public void doClone(BoarUser boarUser, Connection connection) throws SQLException {
        String boarName = BOARS.get(this.curBoarEntry.getKey()).getName();
        int numTryClone = this.interactive.numTryClone;

        List<String> newBoarIDs = new ArrayList<>();
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();
        Set<String> firstBoarIDs = new HashSet<>();

        this.interactive.numClone = boarUser.powQuery().getPowerupAmount(connection, "clone");
        boolean cloneable = RARITIES.get(this.curRarityKey).getAvgClones() != 0 &&
            numTryClone <= this.interactive.numClone;

        if (cloneable) {
            if (boarUser.boarQuery().hasBoar(this.curBoarEntry.getKey(), connection)) {
                int avgClones = RARITIES.get(this.curRarityKey).getAvgClones();
                double chance = numTryClone == avgClones
                    ? 1
                    : (double) (numTryClone % avgClones) / avgClones;
                double randVal = Math.random();

                for (int i = 0; i < (numTryClone / avgClones); i++) {
                    newBoarIDs.add(this.curBoarEntry.getKey());
                }

                if (chance < 1 && chance > randVal) {
                    newBoarIDs.add(this.curBoarEntry.getKey());
                }

                if (newBoarIDs.isEmpty()) {
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                        null,
                        STRS.getCompCloneFailed().formatted(
                            this.interactive.numTryClone == 1
                                ? POWS.get("clone").getName()
                                : POWS.get("clone").getPluralName(),
                            "<>" + this.curRarityKey + "<>" + boarName
                        )
                    );
                } else {
                    boarUser.boarQuery().addBoars(
                        newBoarIDs,
                        connection,
                        BoarObtainType.CLONE.toString(),
                        bucksGotten,
                        editions,
                        firstBoarIDs
                    );

                    boarUser.powQuery().usePowerup(connection, "clone", numTryClone, true);

                    QuestUtil.sendQuestClaimMessage(
                        this.interactive.compEvent.getHook(),
                        boarUser.questQuery().addProgress(QuestType.COLLECT_RARITY, newBoarIDs, connection),
                        boarUser.questQuery().addProgress(QuestType.CLONE_BOARS, newBoarIDs.size(), connection),
                        boarUser.questQuery().addProgress(QuestType.CLONE_RARITY, newBoarIDs, connection)
                    );
                }
            } else {
                this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getCompNoBoar().formatted("<>" + this.curRarityKey + "<>" + boarName)
                );
                Log.debug(this.user, this.getClass(), "Failed to clone: Lack of boar");
            }
        } else {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getNoPow().formatted(POWS.get("transmute").getPluralName())
            );
            Log.debug(this.user, this.getClass(), "Failed to clone: Not enough");
        }

        if (!newBoarIDs.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    this.interactive.boarPage = BOARS.get(this.curBoarEntry.getKey()).getName();
                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                        null, STRS.getCompCloneSuccess().formatted("<>" + this.curRarityKey + "<>" + boarName)
                    );

                    String title = STRS.getCompCloneTitle();

                    ItemInteractive.sendInteractive(
                        newBoarIDs,
                        bucksGotten,
                        editions,
                        firstBoarIDs,
                        null,
                        this.user,
                        title,
                        this.interactive.compEvent.getHook(),
                        true
                    );

                    Log.debug(this.user, this.getClass(), "Sent ItemInteractive");
                } catch (RuntimeException exception) {
                    this.interactive.stop(StopType.EXCEPTION);
                    Log.error(
                        this.user, this.getClass(), "A problem occurred when sending clone item interactive", exception
                    );
                }
            });
        }
    }

    public void doTransmute(BoarUser boarUser, Connection connection) throws SQLException {
        String boarName = BOARS.get(this.curBoarEntry.getKey()).getName();

        List<String> newBoarIDs = new ArrayList<>();
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();
        Set<String> firstBoarIDs = new HashSet<>();

        this.interactive.numTransmute = boarUser.powQuery().getPowerupAmount(connection, "transmute");
        RarityConfig curRarity = RARITIES.get(this.curRarityKey);
        boolean transmutable = curRarity.getChargesNeeded() != 0 &&
            curRarity.getChargesNeeded() <= this.interactive.numTransmute;

        if (transmutable) {
            if (boarUser.boarQuery().hasBoar(this.curBoarEntry.getKey(), connection)) {
                String nextRarityID = BoarUtil.getNextRarityKey(this.curRarityKey);
                newBoarIDs.add(BoarUtil.findValid(nextRarityID, this.interactive.isSkyblockGuild()));

                boarUser.boarQuery().removeBoar(this.curBoarEntry.getKey(), connection);
                boarUser.boarQuery().addBoars(
                    newBoarIDs,
                    connection,
                    BoarObtainType.TRANSMUTE.toString(),
                    bucksGotten,
                    editions,
                    firstBoarIDs
                );

                boarUser.powQuery().usePowerup(connection, "transmute", this.interactive.numTransmute, true);

                QuestUtil.sendQuestClaimMessage(
                    this.interactive.compEvent.getHook(),
                    boarUser.questQuery().addProgress(QuestType.COLLECT_RARITY, newBoarIDs, connection)
                );
            } else {
                this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getCompNoBoar().formatted("<>" + this.curRarityKey + "<>" + boarName)
                );
                Log.debug(this.user, this.getClass(), "Failed to transmute: Lack of boar");
            }
        } else {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getNoPow().formatted(POWS.get("transmute").getPluralName())
            );
            Log.debug(this.user, this.getClass(), "Failed to transmute: Not enough");
        }

        if (!newBoarIDs.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    String newBoarName = BOARS.get(newBoarIDs.getFirst()).getName();
                    String newBoarRarityKey = BoarUtil.findRarityKey(newBoarIDs.getFirst());

                    if (this.interactive.filteredBoars.containsKey(newBoarIDs.getFirst())) {
                        this.interactive.boarPage = newBoarName;
                    }

                    String overlayStr = STRS.getCompTransmuteSuccess().formatted(
                        "<>" + this.curRarityKey + "<>" + boarName,
                        "<>" + newBoarRarityKey + "<>" + newBoarName
                    );

                    this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, overlayStr);

                    if (newBoarIDs.size() > 1) {
                        String firstBoarID = CONFIG.getMainConfig().getFirstBoarID();
                        String firstBoarName = BOARS.get(firstBoarID).getName();
                        String firstRarityKey = BoarUtil.findRarityKey(firstBoarID);

                        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                            null, STRS.getCompTransmuteFirst().formatted("<>" + firstRarityKey + "<>" + firstBoarName)
                        );
                    }

                    String title = STRS.getCompTransmuteTitle();

                    ItemInteractive.sendInteractive(
                        newBoarIDs,
                        bucksGotten,
                        editions,
                        firstBoarIDs,
                        null,
                        this.user,
                        title,
                        this.interactive.compEvent.getHook(),
                        true
                    );

                    Log.debug(this.user, this.getClass(), "Sent ItemInteractive");
                } catch (RuntimeException exception) {
                    this.interactive.stop(StopType.EXCEPTION);
                    Log.error(
                        this.user,
                        this.getClass(),
                        "A problem occurred when sending transmute item interactive",
                        exception
                    );
                }
            });
        }
    }

    public void doCharm(BoarUser boarUser, Connection connection) throws SQLException {
        if (this.interactive.numTryCharm > boarUser.powQuery().getPowerupAmount(connection, "miracle")) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getNoPow().formatted(POWS.get(this.interactive.powerupUsing).getPluralName())
            );
            Log.debug(this.user, this.getClass(), "Failed to miracle: Not enough");
            return;
        }

        boarUser.powQuery().activateMiracles(connection, this.interactive.numTryCharm);

        long blessings = boarUser.baseQuery().getBlessings(connection);
        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
            null,
            STRS.getPowMiracleSuccess().formatted(
                STRS.getBlessingsPluralName(),
                TextUtil.getBlessHex(blessings, true),
                STRS.getBlessingsSymbol() + " ",
                blessings
            )
        );
    }

    public void doGift(BoarUser boarUser, Connection connection) throws SQLException {
        PowerupItemConfig powConfig = POWS.get("gift");
        int giftAmt = boarUser.powQuery().getPowerupAmount(connection, "gift");

        if (giftAmt == 0) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getNoPow().formatted(powConfig.getPluralName())
            );
            Log.debug(this.user, this.getClass(), "Failed to gift: Not enough");
            return;
        }

        long lastGiftSent = boarUser.powQuery().getLastGiftSent(connection);

        if (lastGiftSent > TimeUtil.getCurMilli() - NUMS.getGiftIdle()) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getGiftAlreadySent());
            Log.debug(this.user, this.getClass(), "Failed to gift: Already sent");
            return;
        }

        long bannedTimestamp = boarUser.baseQuery().getBannedTime(connection);

        if (bannedTimestamp > TimeUtil.getCurMilli()) {
            String banStr = STRS.getBannedString().formatted(TimeUtil.getTimeDistance(bannedTimestamp, false));
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, banStr);
            Log.debug(this.user, this.getClass(), "Failed to gift: Banned");
            return;
        }

        boarUser.powQuery().setLastGiftSent(connection, TimeUtil.getCurMilli());

        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
            null, STRS.getPowGiftSuccess().formatted(powConfig.getName())
        );

        CompletableFuture.runAsync(() -> {
            try {
                Interactive giftInteractive = InteractiveFactory.constructGiftInteractive(
                    this.interactive.compEvent, true
                );
                giftInteractive.execute(null);
                Log.debug(this.user, this.getClass(), "Sent BoarGiftInteractive");
            } catch (RuntimeException exception) {
                this.interactive.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "A problem occurred when sending gift interactive", exception);
            }
        });
    }

    public void doQuestClaim(BoarUser boarUser, Connection connection) throws SQLException {
        this.interactive.questData = boarUser.megaQuery().getQuestsData(connection);
        QuestInfo questInfo = boarUser.questQuery().claimQuests(this.interactive.questData, connection);
        String questClaimStr = QuestUtil.getQuestClaimMessage(questInfo);

        this.interactive.acknowledgeOpen = true;

        if (questInfo != null) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, questClaimStr);
        } else {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getQuestNoClaim());
            Log.debug(this.user, this.getClass(), "No quests to claim");
        }
    }

    public void doQuestBonus(BoarUser boarUser, Connection connection) throws SQLException {
        this.interactive.questData = boarUser.megaQuery().getQuestsData(connection);
        boolean claimedBonus = boarUser.questQuery().claimBonus(this.interactive.questData, connection);

        this.interactive.acknowledgeOpen = true;

        if (claimedBonus) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null,
                STRS.getQuestBonusClaimed().formatted(
                    NUMS.getQuestBonusAmt(),
                    NUMS.getQuestBonusAmt() == 1
                        ? POWS.get("transmute").getName()
                        : POWS.get("transmute").getPluralName()
                )
            );
        } else {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getQuestNoBonus());
            Log.debug(this.user, this.getClass(), "No quest bonus to claim");
        }
    }

    public void toggleQuestAuto(BoarUser boarUser, Connection connection) throws SQLException {
        boarUser.questQuery().toggleAutoClaim(connection);
    }
}
