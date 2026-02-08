package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.commands.boar.megamenu.AdventSubcommand;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.AdventData;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.util.boar.BoarTag;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.quests.QuestInfo;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.quests.QuestUtil;
import dev.boarbot.util.resource.ResourceUtil;
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

    public void setFilterBits(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.megaQuery().setFilterBits(connection, this.interactive.filterBits);
            Log.debug(boarUser.getUser(), this.getClass(), "Set filter bits to " + this.interactive.filterBits);
        } catch (SQLException exception) {
            this.interactive.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update filter bits", exception);
        }
    }

    public void setSortVal(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.megaQuery().setSortVal(connection, this.interactive.sortVal);
            Log.debug(boarUser.getUser(), this.getClass(), "Set sort value to " + this.interactive.sortVal);
        } catch (SQLException exception) {
            this.interactive.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update sort value", exception);
        }
    }

    public void doInteract(BoarUser boarUser) {
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
    }

    public void usePowerup(BoarUser boarUser) {
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
    }

    public void doQuestAction(BoarUser boarUser) {
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

    public void claimAdvent(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            this.doAdventClaim(boarUser, connection);
        } catch (SQLException exception) {
            this.interactive.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update advent data", exception);
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

        this.interactive.mustBeExact = true;
        this.interactive.boarPage = boarName;

        this.interactive.numClone = boarUser.powQuery().getPowerupAmount(connection, "clone");

        if (this.interactive.numClone == 0) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getNoItem());

            Log.debug(this.user, this.getClass(), "Failed to transmute: Has none");
            return;
        }

        if (numTryClone > this.interactive.numClone) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null,
                STRS.getSomePow().formatted(
                    numTryClone,
                    numTryClone == 1
                        ? POWS.get("clone").getName()
                        : POWS.get("clone").getPluralName(),
                    this.interactive.numClone
                )
            );

            Log.debug(this.user, this.getClass(), "Failed to clone: Not enough");
            return;
        }

        if (RARITIES.get(this.curRarityKey).getAvgClones() == 0) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getBadPow().formatted(POWS.get("clone").getPluralName())
            );

            Log.debug(this.user, this.getClass(), "Failed to clone: Cannot clone");
            return;
        }

        if (!boarUser.boarQuery().hasBoar(this.curBoarEntry.getKey(), connection)) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getCompNoBoar().formatted("<>" + this.interactive.curRarityKey + "<>" + boarName)
            );

            Log.debug(this.user, this.getClass(), "Failed to clone: Lack of boar");
            return;
        }

        int avgClones = RARITIES.get(this.curRarityKey).getAvgClones();
        double chance = numTryClone == avgClones
            ? 1
            : (double) (numTryClone % avgClones) / avgClones;
        double randVal = Math.random();

        for (int i=0; i<(numTryClone / avgClones); i++) {
            newBoarIDs.add(this.curBoarEntry.getKey());
        }

        if (chance < 1 && chance > randVal) {
            newBoarIDs.add(this.curBoarEntry.getKey());
        }

        boarUser.powQuery().usePowerup(connection, "clone", numTryClone, true);

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

            return;
        }

        boarUser.boarQuery().addBoars(
            newBoarIDs,
            connection,
            BoarTag.CLONE.toString(),
            bucksGotten,
            editions,
            firstBoarIDs
        );

        QuestUtil.sendQuestClaimMessage(
            this.interactive.compEvent.getHook(),
            boarUser.questQuery().addProgress(QuestType.COLLECT_RARITY, newBoarIDs, connection),
            boarUser.questQuery().addProgress(QuestType.CLONE_BOARS, newBoarIDs.size(), connection),
            boarUser.questQuery().addProgress(QuestType.CLONE_RARITY, newBoarIDs, connection)
        );

        CompletableFuture.runAsync(() -> {
            try {
                this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                    null, STRS.getCompCloneSuccess().formatted("<>" + this.curRarityKey + "<>" + boarName)
                );

                this.sendBoarItemInteractive(newBoarIDs, bucksGotten, editions, firstBoarIDs, STRS.getCompCloneTitle());

                Log.debug(this.user, this.getClass(), "Sent ItemInteractive");
            } catch (RuntimeException exception) {
                this.interactive.stop(StopType.EXCEPTION);
                Log.error(
                    this.user, this.getClass(), "A problem occurred when sending clone item interactive", exception
                );
            }
        });
    }

    public void doTransmute(BoarUser boarUser, Connection connection) throws SQLException {
        String boarName = BOARS.get(this.curBoarEntry.getKey()).getName();

        List<String> newBoarIDs = new ArrayList<>();
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();
        Set<String> firstBoarIDs = new HashSet<>();

        this.interactive.mustBeExact = true;
        this.interactive.boarPage = boarName;

        this.interactive.numTransmute = boarUser.powQuery().getPowerupAmount(connection, "transmute");
        RarityConfig curRarity = RARITIES.get(this.curRarityKey);

        if (this.interactive.numTransmute == 0) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getNoItem());

            Log.debug(this.user, this.getClass(), "Failed to transmute: Has none");
            return;
        }

        if (curRarity.getChargesNeeded() > this.interactive.numTransmute) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null,
                STRS.getSomePow().formatted(
                    curRarity.getChargesNeeded(),
                    curRarity.getChargesNeeded() == 1
                        ? POWS.get("transmute").getName()
                        : POWS.get("transmute").getPluralName(),
                    this.interactive.numTransmute
                )
            );

            Log.debug(this.user, this.getClass(), "Failed to transmute: Not enough");
            return;
        }

        if (curRarity.getChargesNeeded() == 0) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getBadPow().formatted(POWS.get("transmute").getPluralName())
            );

            Log.debug(this.user, this.getClass(), "Failed to transmute: Cannot transmute");
            return;
        }

        if (!boarUser.boarQuery().hasBoar(this.curBoarEntry.getKey(), connection)) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null, STRS.getCompNoBoar().formatted("<>" + this.interactive.curRarityKey + "<>" + boarName)
            );

            Log.debug(this.user, this.getClass(), "Failed to transmute: Lack of boar");
            return;
        }

        String nextRarityID = BoarUtil.getNextRarityKey(this.curRarityKey);
        newBoarIDs.add(BoarUtil.findValid(nextRarityID, this.interactive.isSkyblockGuild()));

        boarUser.boarQuery().removeBoar(this.curBoarEntry.getKey(), connection);
        boarUser.boarQuery().addBoars(
            newBoarIDs,
            connection,
            BoarTag.TRANSMUTE.toString(),
            bucksGotten,
            editions,
            firstBoarIDs
        );

        boarUser.powQuery().usePowerup(connection, "transmute", this.interactive.numTransmute, true);

        QuestUtil.sendQuestClaimMessage(
            this.interactive.compEvent.getHook(),
            boarUser.questQuery().addProgress(QuestType.COLLECT_RARITY, newBoarIDs, connection)
        );

        CompletableFuture.runAsync(() -> {
            try {
                String newBoarName = BOARS.get(newBoarIDs.getFirst()).getName();
                String newBoarRarityKey = BoarUtil.findRarityKey(newBoarIDs.getFirst());

                this.interactive.mustBeExact = true;
                this.interactive.boarPage = newBoarName;

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

                this.sendBoarItemInteractive(
                    newBoarIDs, bucksGotten, editions, firstBoarIDs, STRS.getCompTransmuteTitle()
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

    public void doCharm(BoarUser boarUser, Connection connection) throws SQLException {
        int numCharms = boarUser.powQuery().getPowerupAmount(connection, "miracle");

        if (numCharms == 0) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getNoItem());

            Log.debug(this.user, this.getClass(), "Failed to miracle: Has none");
            return;
        }

        if (this.interactive.numTryCharm > numCharms) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(
                null,
                STRS.getSomePow().formatted(
                    this.interactive.numTryCharm,
                    this.interactive.numTryCharm == 1
                        ? POWS.get("miracle").getName()
                        : POWS.get("miracle").getPluralName(),
                    numCharms
                )
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
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getNoItem());

            Log.debug(this.user, this.getClass(), "Failed to gift: Has none");
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

    public void doAdventClaim(BoarUser boarUser, Connection connection) throws SQLException {
        this.interactive.adventData = boarUser.megaQuery().getAdventData(connection);

        if (this.interactive.adventData.adventYear() != TimeUtil.getYear()) {
            this.interactive.adventData = new AdventData(0, TimeUtil.getYear());
        }

        boolean isBonusClaim = this.interactive.adventData.adventBits() == MegaMenuInteractive.FULL_ADVENT_BITS;
        boolean claimable = TimeUtil.getDayOfMonth() <= 25 &&
            (this.interactive.adventData.adventBits() >> (TimeUtil.getDayOfMonth()-1)) % 2 == 0 &&
            this.interactive.adventData.adventBits() < MegaMenuInteractive.FULL_ADVENT_BITS ||
            isBonusClaim;

        this.interactive.acknowledgeOpen = true;

        if (!claimable) {
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getAdventUnavailable());
            Log.debug(this.user, this.getClass(), "No advent rewards to claim");
            return;
        }

        int newBits = this.interactive.adventData.adventBits() + (isBonusClaim
            ? 1
            : (int) Math.pow(2, TimeUtil.getDayOfMonth()-1));

        boarUser.giftQuery().setAdventBits(connection, newBits);

        AdventSubcommand.RewardType adventRewardType = isBonusClaim
            ? AdventSubcommand.RewardType.EVENT
            : AdventSubcommand.getBaseRewardType(TimeUtil.getDayOfMonth());

        String claimStr;

        switch (adventRewardType) {
            case BUCKS -> {
                claimStr = STRS.getAdventBucksClaimed()
                    .formatted(BOARS.get("billionaire").getName(), STRS.getBucksPluralName());
                int randBucks = (int) Math.round(Math.random() * (50 - 30) + 30);
                boarUser.baseQuery().giveBucks(connection, randBucks);

                QuestUtil.sendQuestClaimMessage(
                    this.interactive.compEvent.getHook(),
                    boarUser.questQuery().addProgress(QuestType.COLLECT_BUCKS, randBucks, connection)
                );

                CompletableFuture.runAsync(() ->
                    ItemInteractive.sendInteractive(
                        POWS.get("gift").getOutcomes().get("bucks").getRewardStr()
                            .formatted(randBucks, STRS.getBucksPluralName()),
                        ResourceUtil.bucksGiftPath,
                        "bucks",
                        null,
                        this.user,
                        STRS.getAdventTitle(),
                        false,
                        this.interactive.compEvent.getHook(),
                        true
                    )
                );
            }

            case BLESSINGS -> {
                int otherBlessAmt = 50;
                claimStr = STRS.getAdventBlessingsClaimed()
                    .formatted(BOARS.get("fairy").getName(), otherBlessAmt, STRS.getBlessingsPluralName());
                boarUser.baseQuery().giveOtherBless(connection, otherBlessAmt);
            }

            case CELESTICON -> {
                claimStr = STRS.getAdventCelesticonClaimed().formatted(BOARS.get("boarfadius").getName());

                List<String> boarIDs = BoarUtil.getRandBoarIDs(10000, this.interactive.isSkyblockGuild());
                List<Integer> bucksGotten = new ArrayList<>();
                List<Integer> editions = new ArrayList<>();
                Set<String> firstBoarIDs = new HashSet<>();

                boarUser.boarQuery().addBoars(
                    boarIDs, connection, BoarTag.GIFT.toString(), bucksGotten, editions, firstBoarIDs
                );

                QuestUtil.sendQuestClaimMessage(
                    this.interactive.compEvent.getHook(),
                    boarUser.questQuery().addProgress(QuestType.COLLECT_RARITY, boarIDs, connection)
                );

                CompletableFuture.runAsync(() ->
                    this.sendBoarItemInteractive(boarIDs, bucksGotten, editions, firstBoarIDs, STRS.getAdventTitle())
                );
            }

            case FESTIVE -> {
                claimStr = STRS.getAdventFestiveClaimed()
                    .formatted(BOARS.get("creator").getName(), RARITIES.get("christmas").getName());

                List<String> festiveBoars = Arrays.asList(RARITIES.get("christmas").getBoars());
                int numCurFestive = (int) BOARS.entrySet().stream()
                    .filter(boar -> festiveBoars.contains(boar.getKey()) && boar.getValue().isSecret())
                    .count();

                // Hackily estimates the current festive index based on the number available for the current year
                int curFestiveIndex = (TimeUtil.getDayOfMonth()-1) / (25 / numCurFestive + 1);

                int index = festiveBoars.size() - numCurFestive + curFestiveIndex;
                List<String> boarIDs = new ArrayList<>();
                List<Integer> bucksGotten = new ArrayList<>();
                List<Integer> editions = new ArrayList<>();
                Set<String> firstBoarIDs = new HashSet<>();

                boarIDs.add(RARITIES.get("christmas").getBoars()[index]);

                boarUser.boarQuery().addBoars(
                    boarIDs, connection, BoarTag.GIFT.toString(), bucksGotten, editions, firstBoarIDs
                );

                CompletableFuture.runAsync(() ->
                    this.sendBoarItemInteractive(boarIDs, bucksGotten, editions, firstBoarIDs, STRS.getAdventTitle())
                );
            }

            case EVENT -> {
                claimStr = STRS.getAdventEventClaimed().formatted(BOARS.get("calendar").getName());

                List<String> boarIDs = new ArrayList<>();
                List<Integer> bucksGotten = new ArrayList<>();
                List<Integer> editions = new ArrayList<>();
                Set<String> firstBoarIDs = new HashSet<>();

                boarIDs.add("calendar");

                boarUser.boarQuery().addBoars(
                    boarIDs, connection, BoarTag.GIFT.toString(), bucksGotten, editions, firstBoarIDs
                );

                CompletableFuture.runAsync(() ->
                    this.sendBoarItemInteractive(boarIDs, bucksGotten, editions, firstBoarIDs, STRS.getAdventTitle())
                );
            }

            default -> {
                claimStr = STRS.getAdventPowerupClaimed()
                    .formatted(BOARS.get("santa").getName(), POWS.get("gift").getPluralName());
                int numGifts = 5;
                boarUser.powQuery().addPowerup(connection, "gift", numGifts);

                CompletableFuture.runAsync(() ->
                    ItemInteractive.sendInteractive(
                        POWS.get("gift").getOutcomes().get("powerup").getRewardStr()
                            .formatted(numGifts, POWS.get("gift").getPluralName()),
                        ResourceUtil.powerupAssetsPath + POWS.get("gift").getFile(),
                        "powerup",
                        null,
                        this.user,
                        STRS.getAdventTitle(),
                        false,
                        this.interactive.compEvent.getHook(),
                        true
                    )
                );
            }
        }

        this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, claimStr);
    }

    private void sendBoarItemInteractive(
        List<String> boarIDs, List<Integer> bucksGotten, List<Integer> editions, Set<String> firstBoarIDs, String title
    ) {
        ItemInteractive.sendInteractive(
            boarIDs,
            bucksGotten,
            editions,
            firstBoarIDs,
            null,
            this.user,
            title,
            this.interactive.compEvent.getHook(),
            true
        );
    }
}
