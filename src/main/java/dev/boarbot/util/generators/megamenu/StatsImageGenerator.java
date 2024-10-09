package dev.boarbot.util.generators.megamenu;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.entities.boaruser.data.StatsData;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.resource.ResourceUtil;
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class StatsImageGenerator extends MegaMenuGenerator {
    private static final int VALUE_Y_OFFSET = 65;

    private final StatsData statsData;

    public StatsImageGenerator(
        int page,
        BoarUser boarUser,
        List<BadgeData> badges,
        String firstJoinedDate,
        StatsData statsData
    ) {
        super(page, boarUser, badges, firstJoinedDate);
        this.statsData = statsData;
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.megaMenuBasePath, ORIGIN, IMAGE_SIZE);

        this.textDrawer = new TextDrawer(
            g2d, "", ORIGIN, Align.LEFT, COLORS.get("font"), NUMS.getFontMedium()
        );

        switch (this.page) {
            case 0 -> this.drawPageOne();
            case 1 -> this.drawPageTwo();
            case 2 -> this.drawPageThree();
            case 3 -> this.drawPageFour();
            case 4 -> this.drawPageFive();
            case 5 -> this.drawPageSix();
            case 6 -> this.drawPageSeven();
            case 7 -> this.drawPageEight();
        }

        this.drawTopInfo();
        return this;
    }

    private void drawPageOne() {
        final int LEFT_START_X = 335;
        final int RIGHT_START_X = 1075;
        final int START_Y = 359;
        final int LABEL_Y_SPACING = 145;

        int[] bucksLabelPos = {LEFT_START_X, START_Y};
        String bucksStr = "<>bucks<>$%,d".formatted(this.statsData.bucks());
        int[] bucksPos = {LEFT_START_X, bucksLabelPos[1] + VALUE_Y_OFFSET};

        String peakBucksLabel = STRS.getStatsPeakLabel().formatted(STRS.getBucksPluralName());
        int[] peakBucksLabelPos = {LEFT_START_X, bucksLabelPos[1] + LABEL_Y_SPACING};
        String peakBucksStr = "<>bucks<>$%,d".formatted(this.statsData.highestBucks());
        int[] peakBucksPos = {LEFT_START_X, peakBucksLabelPos[1] + VALUE_Y_OFFSET};

        int[] dailiesLabelPos = {LEFT_START_X, peakBucksLabelPos[1] + LABEL_Y_SPACING};
        String dailiesStr = "%,d".formatted(this.statsData.dailies());
        int[] dailiesPos = {LEFT_START_X, dailiesLabelPos[1] + VALUE_Y_OFFSET};

        int[] dailiesMissedLabelPos = {LEFT_START_X, dailiesLabelPos[1] + LABEL_Y_SPACING};
        String dailiesMissedStr = "%,d".formatted(this.statsData.dailiesMissed());
        int[] dailiesMissedPos = {LEFT_START_X, dailiesMissedLabelPos[1] + VALUE_Y_OFFSET};

        int[] lastDailyLabelPos = {LEFT_START_X, dailiesMissedLabelPos[1] + LABEL_Y_SPACING};
        String lastDailyStr = this.statsData.lastDailyTimestamp() == null
            ? STRS.getUnavailable()
            : Instant.ofEpochMilli(this.statsData.lastDailyTimestamp().getTime())
                .atOffset(ZoneOffset.UTC)
                .format(TimeUtil.getDateFormatter());
        int[] lastDailyPos = {LEFT_START_X, lastDailyLabelPos[1] + VALUE_Y_OFFSET};

        int[] lastBoarLabelPos = {LEFT_START_X, lastDailyLabelPos[1] + LABEL_Y_SPACING};
        String lastBoarStr = this.statsData.lastBoar() == null
            ? STRS.getUnavailable()
            : "<>%s<>".formatted(BoarUtil.findRarityKey(this.statsData.lastBoar())) +
                BOARS.get(this.statsData.lastBoar()).getName();
        int[] lastBoarPos = {LEFT_START_X, lastBoarLabelPos[1] + VALUE_Y_OFFSET};

        int[] favBoarLabelPos = {LEFT_START_X, lastBoarLabelPos[1] + LABEL_Y_SPACING};
        String favBoarStr = this.statsData.favBoar() == null
            ? STRS.getUnavailable()
            : "<>%s<>".formatted(BoarUtil.findRarityKey(this.statsData.favBoar())) +
                BOARS.get(this.statsData.favBoar()).getName();
        int[] favBoarPos = {LEFT_START_X, favBoarLabelPos[1] + VALUE_Y_OFFSET};

        String totalLabel = STRS.getStatsTotalLabel().formatted(STRS.getMainItemPluralName());
        int[] totalLabelPos = {RIGHT_START_X, START_Y};
        String totalStr = "%,d".formatted(this.statsData.totalBoars());
        int[] totalPos = {RIGHT_START_X, totalLabelPos[1] + VALUE_Y_OFFSET};

        String peakTotalLabel = STRS.getStatsPeakLabel().formatted(totalLabel);
        int[] peakTotalLabelPos = {RIGHT_START_X, totalLabelPos[1] + LABEL_Y_SPACING};
        String peakTotalStr = "%,d".formatted(this.statsData.highestBoars());
        int[] peakTotalPos = {RIGHT_START_X, peakTotalLabelPos[1] + VALUE_Y_OFFSET};

        int[] uniquesLabelPos = {RIGHT_START_X, peakTotalLabelPos[1] + LABEL_Y_SPACING};
        String uniquesStr = "%,d".formatted(this.statsData.uniques());
        int[] uniquesPos = {RIGHT_START_X, uniquesLabelPos[1] + VALUE_Y_OFFSET};

        String peakUniquesLabel = STRS.getStatsPeakLabel().formatted(STRS.getStatsUniquesLabel());
        int[] peakUniquesLabelPos = {RIGHT_START_X, uniquesLabelPos[1] + LABEL_Y_SPACING};
        String peakUniquesStr = "%,d".formatted(this.statsData.highestUniques());
        int[] peakUniquesPos = {RIGHT_START_X, peakUniquesLabelPos[1] + VALUE_Y_OFFSET};

        int[] streakLabelPos = {RIGHT_START_X, peakUniquesLabelPos[1] + LABEL_Y_SPACING};
        String streakStr = "%,d".formatted(this.statsData.boarStreak());
        int[] streakPos = {RIGHT_START_X, streakLabelPos[1] + VALUE_Y_OFFSET};

        String peakStreakLabel = STRS.getStatsPeakLabel().formatted(STRS.getStatsStreakLabel());
        int[] peakStreakLabelPos = {RIGHT_START_X, streakLabelPos[1] + LABEL_Y_SPACING};
        String peakStreakStr = "%,d".formatted(this.statsData.highestStreak());
        int[] peakStreakPos = {RIGHT_START_X, peakStreakLabelPos[1] + VALUE_Y_OFFSET};

        int[] notificationLabelPos = {RIGHT_START_X, peakStreakLabelPos[1] + LABEL_Y_SPACING};
        String notificationStr = this.statsData.notificationsOn()
            ? "<>green<>ENABLED"
            : "<>error<>DISABLED";
        int[] notificationPos = {RIGHT_START_X, notificationLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, STRS.getBucksPluralName(), bucksLabelPos);
        TextUtil.drawValue(this.textDrawer, bucksStr, bucksPos, true);

        TextUtil.drawLabel(this.textDrawer, peakBucksLabel, peakBucksLabelPos);
        TextUtil.drawValue(this.textDrawer, peakBucksStr, peakBucksPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsDailiesLabel(), dailiesLabelPos);
        TextUtil.drawValue(this.textDrawer, dailiesStr, dailiesPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsDailiesMissedLabel(), dailiesMissedLabelPos);
        TextUtil.drawValue(this.textDrawer, dailiesMissedStr, dailiesMissedPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsLastDailyLabel(), lastDailyLabelPos);
        TextUtil.drawValue(this.textDrawer, lastDailyStr, lastDailyPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsLastBoarLabel(), lastBoarLabelPos);
        TextUtil.drawValue(this.textDrawer, lastBoarStr, lastBoarPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsFavBoarLabel(), favBoarLabelPos);
        TextUtil.drawValue(this.textDrawer, favBoarStr, favBoarPos, true);

        TextUtil.drawLabel(this.textDrawer, totalLabel, totalLabelPos);
        TextUtil.drawValue(this.textDrawer, totalStr, totalPos, true);

        TextUtil.drawLabel(this.textDrawer, peakTotalLabel, peakTotalLabelPos);
        TextUtil.drawValue(this.textDrawer, peakTotalStr, peakTotalPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsUniquesLabel(), uniquesLabelPos);
        TextUtil.drawValue(this.textDrawer, uniquesStr, uniquesPos, true);

        TextUtil.drawLabel(this.textDrawer, peakUniquesLabel, peakUniquesLabelPos);
        TextUtil.drawValue(this.textDrawer, peakUniquesStr, peakUniquesPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsStreakLabel(), streakLabelPos);
        TextUtil.drawValue(this.textDrawer, streakStr, streakPos, true);

        TextUtil.drawLabel(this.textDrawer, peakStreakLabel, peakStreakLabelPos);
        TextUtil.drawValue(this.textDrawer, peakStreakStr, peakStreakPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsNotificationLabel(), notificationLabelPos);
        TextUtil.drawValue(this.textDrawer, notificationStr, notificationPos, true);
    }

    private void drawPageTwo() {
        final int LEFT_START_X = 240;
        final int RIGHT_START_X = 980;
        final int START_Y = 504;
        final int LABEL_Y_SPACING = 145;

        int[] blessingsLabelPos = {LEFT_START_X, START_Y};
        String blessHex = TextUtil.getBlessHex(this.statsData.blessings(), this.statsData.miraclesActive() > 0);
        String blessingsStr = this.statsData.miraclesActive() > 0
            ? "%s %,d".formatted(STRS.getBlessingsSymbol(), this.statsData.blessings())
            : "%,d".formatted(this.statsData.blessings());
        int[] blessingsPos = {LEFT_START_X, blessingsLabelPos[1] + VALUE_Y_OFFSET};

        String streakBlessLabel = STRS.getStatsBlessLabel().formatted(
            STRS.getBlessingsPluralNameShortened(), STRS.getBlessCategory1()
        );
        int[] streakBlessLabelPos = {LEFT_START_X, blessingsLabelPos[1] + LABEL_Y_SPACING};
        String streakBlessStr = this.statsData.streakBless() == NUMS.getMaxStreakBless()
            ? "<>gold<>" + this.statsData.streakBless()
            : Integer.toString(this.statsData.streakBless());
        int[] streakBlessPos = {LEFT_START_X, streakBlessLabelPos[1] + VALUE_Y_OFFSET};

        String questBlessLabel = STRS.getStatsBlessLabel().formatted(
            STRS.getBlessingsPluralNameShortened(), STRS.getBlessCategory2()
        );
        int[] questBlessLabelPos = {LEFT_START_X, streakBlessLabelPos[1] + LABEL_Y_SPACING};
        String questBlessStr = this.statsData.questBless() == NUMS.getMaxQuestBless()
            ? "<>gold<>" + this.statsData.questBless()
            : Integer.toString(this.statsData.questBless());
        int[] questBlessPos = {LEFT_START_X, questBlessLabelPos[1] + VALUE_Y_OFFSET};

        String uniqueBlessLabel = STRS.getStatsBlessLabel().formatted(
            STRS.getBlessingsPluralNameShortened(), STRS.getBlessCategory3()
        );
        int[] uniqueBlessLabelPos = {LEFT_START_X, questBlessLabelPos[1] + LABEL_Y_SPACING};
        String uniqueBlessStr = this.statsData.uniqueBless() == NUMS.getMaxUniqueBless()
            ? "<>gold<>" + this.statsData.uniqueBless()
            : Integer.toString(this.statsData.uniqueBless());
        int[] uniqueBlessPos = {LEFT_START_X, uniqueBlessLabelPos[1] + VALUE_Y_OFFSET};

        String otherBlessLabel = STRS.getStatsBlessLabel().formatted(
            STRS.getBlessingsPluralNameShortened(), STRS.getBlessCategory4()
        );
        int[] otherBlessLabelPos = {LEFT_START_X, uniqueBlessLabelPos[1] + LABEL_Y_SPACING};
        String otherBlessStr = this.statsData.otherBless() == NUMS.getMaxOtherBless()
            ? "<>gold<>" + this.statsData.otherBless()
            : Integer.toString(this.statsData.otherBless());
        int[] otherBlessPos = {LEFT_START_X, otherBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakBlessingsLabel = STRS.getStatsPeakLabel().formatted(
            STRS.getBlessingsPluralName()
        );
        int[] peakBlessingsLabelPos = {RIGHT_START_X, START_Y};
        String peakBlessHex = TextUtil.getBlessHex(this.statsData.highestBlessings(), false);
        String peakBlessingsStr = this.statsData.highestBlessings() > 1000
            ? "%s %,d".formatted(STRS.getBlessingsSymbol(), this.statsData.highestBlessings())
            : "%,d".formatted(this.statsData.highestBlessings());
        int[] peakBlessingsPos = {RIGHT_START_X, peakBlessingsLabelPos[1] + VALUE_Y_OFFSET};

        String peakStreakBlessLabel = STRS.getStatsPeakLabel().formatted(streakBlessLabel);
        int[] peakStreakBlessLabelPos = {RIGHT_START_X, peakBlessingsLabelPos[1] + LABEL_Y_SPACING};
        String peakStreakBlessStr = this.statsData.highestStreakBless() == NUMS.getMaxStreakBless()
            ? "<>gold<>" + this.statsData.highestStreakBless()
            : Integer.toString(this.statsData.highestStreakBless());
        int[] peakStreakBlessPos = {RIGHT_START_X, peakStreakBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakQuestBlessLabel = STRS.getStatsPeakLabel().formatted(questBlessLabel);
        int[] peakQuestBlessLabelPos = {RIGHT_START_X, peakStreakBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakQuestBlessStr = this.statsData.highestQuestBless() == NUMS.getMaxQuestBless()
            ? "<>gold<>" + this.statsData.highestQuestBless()
            : Integer.toString(this.statsData.highestQuestBless());
        int[] peakQuestBlessPos = {RIGHT_START_X, peakQuestBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakUniqueBlessLabel = STRS.getStatsPeakLabel().formatted(uniqueBlessLabel);
        int[] peakUniqueBlessLabelPos = {RIGHT_START_X, questBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakUniqueBlessStr = this.statsData.highestUniqueBless() == NUMS.getMaxUniqueBless()
            ? "<>gold<>" + this.statsData.highestUniqueBless()
            : Integer.toString(this.statsData.highestUniqueBless());
        int[] peakUniqueBlessPos = {RIGHT_START_X, peakUniqueBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakOtherBlessLabel = STRS.getStatsPeakLabel().formatted(otherBlessLabel);
        int[] peakOtherBlessLabelPos = {RIGHT_START_X, peakUniqueBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakOtherBlessStr = this.statsData.highestOtherBless() == NUMS.getMaxOtherBless()
            ? "<>gold<>" + this.statsData.highestOtherBless()
            : Integer.toString(this.statsData.highestOtherBless());
        int[] peakOtherBlessPos = {RIGHT_START_X, peakOtherBlessLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, STRS.getBlessingsPluralName(), blessingsLabelPos);
        TextUtil.drawValue(this.textDrawer, blessingsStr, blessingsPos, true, blessHex);

        TextUtil.drawLabel(this.textDrawer, streakBlessLabel, streakBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, streakBlessStr, streakBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, questBlessLabel, questBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, questBlessStr, questBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, uniqueBlessLabel, uniqueBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, uniqueBlessStr, uniqueBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, otherBlessLabel, otherBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, otherBlessStr, otherBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, peakBlessingsLabel, peakBlessingsLabelPos);
        TextUtil.drawValue(this.textDrawer, peakBlessingsStr, peakBlessingsPos, true, peakBlessHex);

        TextUtil.drawLabel(this.textDrawer, peakStreakBlessLabel, peakStreakBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakStreakBlessStr, peakStreakBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, peakQuestBlessLabel, peakQuestBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakQuestBlessStr, peakQuestBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, peakUniqueBlessLabel, peakUniqueBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakUniqueBlessStr, peakUniqueBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, peakOtherBlessLabel, peakOtherBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakOtherBlessStr, peakOtherBlessPos, true);
    }

    private void drawPageThree() {
        final int LEFT_START_X = 327;
        final int RIGHT_START_X = 1067;
        final int START_Y = 577;
        final int LABEL_Y_SPACING = 145;

        int[] powAttemptsLabelPos = {LEFT_START_X, START_Y};
        String powAttemptsStr = "%,d".formatted(this.statsData.powerupAttempts());
        int[] powAttemptsPos = {LEFT_START_X, powAttemptsLabelPos[1] + VALUE_Y_OFFSET};

        int[] powWinsLabelPos = {LEFT_START_X, powAttemptsLabelPos[1] + LABEL_Y_SPACING};
        String powWinsStr = "%,d".formatted(this.statsData.powerupWins());
        int[] powWinsPos = {LEFT_START_X, powWinsLabelPos[1] + VALUE_Y_OFFSET};

        int[] powPerfectLabelPos = {LEFT_START_X, powWinsLabelPos[1] + LABEL_Y_SPACING};
        String powPerfectStr = "%,d".formatted(this.statsData.perfectPowerups());
        int[] powPerfectPos = {LEFT_START_X, powPerfectLabelPos[1] + VALUE_Y_OFFSET};

        int[] powFastLabelPos = {LEFT_START_X, powPerfectLabelPos[1] + LABEL_Y_SPACING};
        String powFastStr = this.statsData.fastestPowerup() == 120000
            ? STRS.getUnavailable()
            : "%,dms".formatted(this.statsData.fastestPowerup());
        int[] powFastPos = {LEFT_START_X, powFastLabelPos[1] + VALUE_Y_OFFSET};

        int[] powAvgLabelPos = {RIGHT_START_X, START_Y};
        String powAvgStr = this.statsData.powerupWins() == 0
            ? STRS.getUnavailable()
            : "Top %,.2f%%".formatted(this.statsData.avgPowerupPlacement() * 100);
        int[] powAvgPos = {RIGHT_START_X, powAvgLabelPos[1] + VALUE_Y_OFFSET};

        int[] powBestOneLabelPos = {RIGHT_START_X, powAvgLabelPos[1] + LABEL_Y_SPACING};
        String powBestOneStr = this.statsData.bestPrompts().isEmpty()
            ? STRS.getUnavailable()
            : BoarUtil.getPromptStr(this.statsData.bestPrompts().getFirst());
        int[] powBestOnePos = {RIGHT_START_X, powBestOneLabelPos[1] + VALUE_Y_OFFSET};

        int[] powBestTwoLabelPos = {RIGHT_START_X, powBestOneLabelPos[1] + LABEL_Y_SPACING};
        String powBestTwoStr = this.statsData.bestPrompts().size() < 2
            ? STRS.getUnavailable()
            : BoarUtil.getPromptStr(this.statsData.bestPrompts().get(1));
        int[] powBestTwoPos = {RIGHT_START_X, powBestTwoLabelPos[1] + VALUE_Y_OFFSET};

        int[] powBestThreeLabelPos = {RIGHT_START_X, powBestTwoLabelPos[1] + LABEL_Y_SPACING};
        String powBestThreeStr = this.statsData.bestPrompts().size() < 3
            ? STRS.getUnavailable()
            : BoarUtil.getPromptStr(this.statsData.bestPrompts().get(2));
        int[] powBestThreePos = {RIGHT_START_X, powBestThreeLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowAttemptsLabel(), powAttemptsLabelPos);
        TextUtil.drawValue(this.textDrawer, powAttemptsStr, powAttemptsPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowWinsLabel(), powWinsLabelPos);
        TextUtil.drawValue(this.textDrawer, powWinsStr, powWinsPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowPerfectLabel(), powPerfectLabelPos);
        TextUtil.drawValue(this.textDrawer, powPerfectStr, powPerfectPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowFastestLabel(), powFastLabelPos);
        TextUtil.drawValue(this.textDrawer, powFastStr, powFastPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowAvgLabel(), powAvgLabelPos);
        TextUtil.drawValue(this.textDrawer, powAvgStr, powAvgPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowBestLabel1(), powBestOneLabelPos);
        TextUtil.drawValue(this.textDrawer, powBestOneStr, powBestOnePos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowBestLabel2(), powBestTwoLabelPos);
        TextUtil.drawValue(this.textDrawer, powBestTwoStr, powBestTwoPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPowBestLabel3(), powBestThreeLabelPos);
        TextUtil.drawValue(this.textDrawer, powBestThreeStr, powBestThreePos, true);
    }

    private void drawPageFour() {
        final int LEFT_START_X = 377;
        final int RIGHT_START_X = 1117;
        final int START_Y = 577;
        final int LABEL_Y_SPACING = 145;

        PowerupItemConfig pow = POWS.get("miracle");

        String miracleAmtLabel = STRS.getStatsTotalLabel().formatted(pow.getShortPluralName());
        int[] miracleAmtLabelPos = {LEFT_START_X, START_Y};
        String miracleAmtStr = this.statsData.powAmts().get("miracle") == null
            ? "0"
            : "%,d".formatted(this.statsData.powAmts().get("miracle"));
        int[] miracleAmtPos = {LEFT_START_X, miracleAmtLabelPos[1] + VALUE_Y_OFFSET};

        String peakMiracleAmtLabel = STRS.getStatsPeakLabel().formatted(pow.getShortPluralName());
        int[] peakMiracleAmtLabelPos = {LEFT_START_X, miracleAmtLabelPos[1] + LABEL_Y_SPACING};
        String peakMiracleAmtStr = this.statsData.peakPowAmts().get("miracle") == null
            ? "0"
            : "%,d".formatted(this.statsData.peakPowAmts().get("miracle"));
        int[] peakMiracleAmtPos = {LEFT_START_X, peakMiracleAmtLabelPos[1] + VALUE_Y_OFFSET};

        int[] miraclesActiveLabelPos = {LEFT_START_X, peakMiracleAmtLabelPos[1] + LABEL_Y_SPACING};
        String miraclesActiveStr = "%,d".formatted(this.statsData.miraclesActive());
        int[] miraclesActivePos = {LEFT_START_X, miraclesActiveLabelPos[1] + VALUE_Y_OFFSET};

        String miraclesUsedLabel = STRS.getStatsUsedLabel().formatted(pow.getShortPluralName());
        int[] miraclesUsedLabelPos = {LEFT_START_X, miraclesActiveLabelPos[1] + LABEL_Y_SPACING};
        String miraclesUsedStr = this.statsData.powUsed().get("miracle") == null
            ? "0"
            : "%,d".formatted(this.statsData.powUsed().get("miracle"));
        int[] miraclesUsedPos = {LEFT_START_X, miraclesUsedLabelPos[1] + VALUE_Y_OFFSET};

        int[] miracleRollsLabelPos = {RIGHT_START_X, START_Y};
        String miracleRollsStr = "%,d".formatted(this.statsData.miracleRolls());
        int[] miracleRollsPos = {RIGHT_START_X, miracleRollsLabelPos[1] + VALUE_Y_OFFSET};

        int[] biggestRollLabelPos = {RIGHT_START_X, miracleRollsLabelPos[1] + LABEL_Y_SPACING};
        String biggestRollStr = this.statsData.miraclesMostUsed() == 0
            ? STRS.getUnavailable()
            : "%,d".formatted(this.statsData.miraclesMostUsed());
        int[] biggestRollPos = {RIGHT_START_X, biggestRollLabelPos[1] + VALUE_Y_OFFSET};

        int[] bestBucksLabelPos = {RIGHT_START_X, biggestRollLabelPos[1] + LABEL_Y_SPACING};
        String bestBucksStr = this.statsData.miracleBestBucks() == 0
            ? STRS.getUnavailable()
            : "<>bucks<>$%,d".formatted(this.statsData.miracleBestBucks());
        int[] bestBucksPos = {RIGHT_START_X, bestBucksLabelPos[1] + VALUE_Y_OFFSET};

        int[] bestRarityLabelPos = {RIGHT_START_X, bestBucksLabelPos[1] + LABEL_Y_SPACING};
        String bestRarityStr = this.statsData.miracleBestRarity() == null
            ? STRS.getUnavailable()
            : "<>" + this.statsData.miracleBestRarity() + "<>" +
                RARITIES.get(this.statsData.miracleBestRarity()).getName();
        int[] bestRarityPos = {RIGHT_START_X, bestRarityLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, miracleAmtLabel, miracleAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, miracleAmtStr, miracleAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, peakMiracleAmtLabel, peakMiracleAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, peakMiracleAmtStr, peakMiracleAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsMiraclesActiveLabel(), miraclesActiveLabelPos);
        TextUtil.drawValue(this.textDrawer, miraclesActiveStr, miraclesActivePos, true);

        TextUtil.drawLabel(this.textDrawer, miraclesUsedLabel, miraclesUsedLabelPos);
        TextUtil.drawValue(this.textDrawer, miraclesUsedStr, miraclesUsedPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsMiracleRollsLabel(), miracleRollsLabelPos);
        TextUtil.drawValue(this.textDrawer, miracleRollsStr, miracleRollsPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsPeakMiracleRollLabel(), biggestRollLabelPos);
        TextUtil.drawValue(this.textDrawer, biggestRollStr, biggestRollPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsBestBucksLabel(), bestBucksLabelPos);
        TextUtil.drawValue(this.textDrawer, bestBucksStr, bestBucksPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsBestRarityLabel(), bestRarityLabelPos);
        TextUtil.drawValue(this.textDrawer, bestRarityStr, bestRarityPos, true);
    }

    private void drawPageFive() {
        final int LEFT_START_X = 244;
        final int RIGHT_START_X = 984;
        final int LEFT_START_Y = 504;
        final int RIGHT_START_Y = 355;
        final int LABEL_Y_SPACING = 145;

        PowerupItemConfig pow = POWS.get("transmute");

        String transmuteAmtLabel = STRS.getStatsTotalLabel().formatted(pow.getShortPluralName());
        int[] transmuteAmtLabelPos = {LEFT_START_X, LEFT_START_Y};
        String transmuteAmtStr = this.statsData.powAmts().get("transmute") == null
            ? "0"
            : "%,d".formatted(this.statsData.powAmts().get("transmute"));
        int[] transmuteAmtPos = {LEFT_START_X, transmuteAmtLabelPos[1] + VALUE_Y_OFFSET};

        String peakTransmuteAmtLabel = STRS.getStatsPeakLabel().formatted(pow.getShortPluralName());
        int[] peakTransmuteAmtLabelPos = {LEFT_START_X, transmuteAmtLabelPos[1] + LABEL_Y_SPACING};
        String peakTransmuteAmtStr = this.statsData.peakPowAmts().get("transmute") == null
            ? "0"
            : "%,d".formatted(this.statsData.peakPowAmts().get("transmute"));
        int[] peakTransmuteAmtPos = {LEFT_START_X, peakTransmuteAmtLabelPos[1] + VALUE_Y_OFFSET};

        String transmuteUsedLabel = STRS.getStatsUsedLabel().formatted(pow.getShortPluralName());
        int[] transmuteUsedLabelPos = {LEFT_START_X, peakTransmuteAmtLabelPos[1] + LABEL_Y_SPACING};
        String transmuteUsedStr = this.statsData.powUsed().get("transmute") == null
            ? "0"
            : "%,d".formatted(this.statsData.powUsed().get("transmute"));
        int[] transmuteUsedPos = {LEFT_START_X, transmuteUsedLabelPos[1] + VALUE_Y_OFFSET};

        String lastTransmuteLabel = STRS.getStatsTransmuteLastLabel();
        int[] lastTransmuteLabelPos = {LEFT_START_X, transmuteUsedLabelPos[1] + LABEL_Y_SPACING};
        String lastTransmuteStr = this.statsData.lastTransmuteBoar() == null
            ? STRS.getUnavailable()
            : "<>%s<>%s".formatted(
                BoarUtil.findRarityKey(this.statsData.lastTransmuteBoar()),
                BOARS.get(this.statsData.lastTransmuteBoar()).getName()
        );
        int[] lastTransmutePos = {LEFT_START_X, lastTransmuteLabelPos[1] + VALUE_Y_OFFSET};

        String totalTransmutedLabel = STRS.getStatsTotalLabel()
            .formatted(STRS.getStatsTransmutedLabel().formatted(""));
        int[] totalTransmutedLabelPos = {LEFT_START_X, lastTransmuteLabelPos[1] + LABEL_Y_SPACING};
        String totalTransmutedStr = "%,d".formatted(
            this.statsData.rarityTransmutes().values().stream().reduce(0, Integer::sum)
        );
        int[] totalTransmutedPos = {LEFT_START_X, totalTransmutedLabelPos[1] + VALUE_Y_OFFSET};

        int curY = RIGHT_START_Y;

        for (String rarityKey : RARITIES.keySet()) {
            RarityConfig rarityConfig = RARITIES.get(rarityKey);

            if (rarityConfig.getChargesNeeded() == 0) {
                continue;
            }

            String transmutedRarityLabel = STRS.getStatsTransmutedLabel()
                .formatted("<>" + rarityKey + "<>" + rarityConfig.getPluralName());
            int[] transmutedRarityLabelPos = {RIGHT_START_X, curY};
            String transmutedRarityStr = this.statsData.rarityTransmutes().get(rarityKey) == null
                ? "0"
                : "%,d".formatted(this.statsData.rarityTransmutes().get(rarityKey));
            int[] transmutedRarityPos = {RIGHT_START_X, curY + VALUE_Y_OFFSET};

            TextUtil.drawLabel(this.textDrawer, transmutedRarityLabel, transmutedRarityLabelPos);
            TextUtil.drawValue(this.textDrawer, transmutedRarityStr, transmutedRarityPos, true);

            curY += LABEL_Y_SPACING;
        }

        TextUtil.drawLabel(this.textDrawer, transmuteAmtLabel, transmuteAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, transmuteAmtStr, transmuteAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, peakTransmuteAmtLabel, peakTransmuteAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, peakTransmuteAmtStr, peakTransmuteAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, transmuteUsedLabel, transmuteUsedLabelPos);
        TextUtil.drawValue(this.textDrawer, transmuteUsedStr, transmuteUsedPos, true);

        TextUtil.drawLabel(this.textDrawer, lastTransmuteLabel, lastTransmuteLabelPos);
        TextUtil.drawValue(this.textDrawer, lastTransmuteStr, lastTransmutePos, true);

        TextUtil.drawLabel(this.textDrawer, totalTransmutedLabel, totalTransmutedLabelPos);
        TextUtil.drawValue(this.textDrawer, totalTransmutedStr, totalTransmutedPos, true);
    }

    private void drawPageSix() {
        final int LEFT_START_X = 118;
        final int MIDDLE_START_X = 618;
        final int RIGHT_START_X = 1252;
        final int EDGE_START_Y = 504;
        final int MIDDLE_START_Y = 431;
        final int LABEL_Y_SPACING = 145;

        PowerupItemConfig pow = POWS.get("clone");

        String cloneAmtLabel = STRS.getStatsTotalLabel().formatted(pow.getShortPluralName());
        int[] cloneAmtLabelPos = {LEFT_START_X, EDGE_START_Y};
        String cloneAmtStr = this.statsData.powAmts().get("clone") == null
            ? "0"
            : "%,d".formatted(this.statsData.powAmts().get("clone"));
        int[] cloneAmtPos = {LEFT_START_X, cloneAmtLabelPos[1] + VALUE_Y_OFFSET};

        String peakCloneAmtLabel = STRS.getStatsPeakLabel().formatted(pow.getShortPluralName());
        int[] peakCloneAmtLabelPos = {LEFT_START_X, cloneAmtLabelPos[1] + LABEL_Y_SPACING};
        String peakCloneAmtStr = this.statsData.peakPowAmts().get("clone") == null
            ? "0"
            : "%,d".formatted(this.statsData.peakPowAmts().get("clone"));
        int[] peakCloneAmtPos = {LEFT_START_X, peakCloneAmtLabelPos[1] + VALUE_Y_OFFSET};

        String clonesUsedLabel = STRS.getStatsUsedLabel().formatted(pow.getShortPluralName());
        int[] clonesUsedLabelPos = {LEFT_START_X, peakCloneAmtLabelPos[1] + LABEL_Y_SPACING};
        String clonesUsedStr = this.statsData.powUsed().get("clone") == null
            ? "0"
            : "%,d".formatted(this.statsData.powUsed().get("clone"));
        int[] clonesUsedPos = {LEFT_START_X, clonesUsedLabelPos[1] + VALUE_Y_OFFSET};

        String lastCloneLabel = STRS.getStatsCloneLastLabel();
        int[] lastCloneLabelPos = {LEFT_START_X, clonesUsedLabelPos[1] + LABEL_Y_SPACING};
        String lastCloneStr = this.statsData.lastCloneBoar() == null
            ? STRS.getUnavailable()
            : "<>%s<>%s".formatted(
                BoarUtil.findRarityKey(this.statsData.lastCloneBoar()),
                BOARS.get(this.statsData.lastCloneBoar()).getName()
        );
        int[] lastClonePos = {LEFT_START_X, lastCloneLabelPos[1] + VALUE_Y_OFFSET};

        String totalClonedLabel = STRS.getStatsTotalLabel()
            .formatted(STRS.getStatsClonedLabel().formatted(""));
        int[] totalClonedLabelPos = {LEFT_START_X, lastCloneLabelPos[1] + LABEL_Y_SPACING};
        String totalClonedStr = "%,d".formatted(
            this.statsData.rarityClones().values().stream().reduce(0, Integer::sum)
        );
        int[] totalClonedPos = {LEFT_START_X, totalClonedLabelPos[1] + VALUE_Y_OFFSET};

        int curY = MIDDLE_START_Y;
        int curCount = 1;

        for (String rarityKey : RARITIES.keySet()) {
            RarityConfig rarityConfig = RARITIES.get(rarityKey);

            if (rarityConfig.getAvgClones() == 0) {
                continue;
            }

            int curX = curCount <= 6
                ? MIDDLE_START_X
                : RIGHT_START_X;

            String clonedRarityLabel = STRS.getStatsClonedLabel()
                .formatted("<>" + rarityKey + "<>" + rarityConfig.getPluralName());
            int[] clonedRarityLabelPos = {curX, curY};
            String clonedRarityStr = this.statsData.rarityClones().get(rarityKey) == null
                ? "0"
                : "%,d".formatted(this.statsData.rarityClones().get(rarityKey));
            int[] clonedRarityPos = {curX, curY + VALUE_Y_OFFSET};

            TextUtil.drawLabel(this.textDrawer, clonedRarityLabel, clonedRarityLabelPos);
            TextUtil.drawValue(this.textDrawer, clonedRarityStr, clonedRarityPos, true);

            if (curCount == 6) {
                curY = EDGE_START_Y;
            } else {
                curY += LABEL_Y_SPACING;
            }

            curCount++;
        }

        TextUtil.drawLabel(this.textDrawer, cloneAmtLabel, cloneAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, cloneAmtStr, cloneAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, peakCloneAmtLabel, peakCloneAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, peakCloneAmtStr, peakCloneAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, clonesUsedLabel, clonesUsedLabelPos);
        TextUtil.drawValue(this.textDrawer, clonesUsedStr, clonesUsedPos, true);

        TextUtil.drawLabel(this.textDrawer, lastCloneLabel, lastCloneLabelPos);
        TextUtil.drawValue(this.textDrawer, lastCloneStr, lastClonePos, true);

        TextUtil.drawLabel(this.textDrawer, totalClonedLabel, totalClonedLabelPos);
        TextUtil.drawValue(this.textDrawer, totalClonedStr, totalClonedPos, true);
    }

    private void drawPageSeven() {
        final int LEFT_START_X = 377;
        final int RIGHT_START_X = 1117;
        final int START_Y = 577;
        final int LABEL_Y_SPACING = 145;

        PowerupItemConfig pow = POWS.get("gift");

        String giftAmtLabel = STRS.getStatsTotalLabel().formatted(pow.getShortPluralName());
        int[] giftAmtLabelPos = {LEFT_START_X, START_Y};
        String giftAmtStr = this.statsData.powAmts().get("gift") == null
            ? "0"
            : "%,d".formatted(this.statsData.powAmts().get("gift"));
        int[] giftAmtPos = {LEFT_START_X, giftAmtLabelPos[1] + VALUE_Y_OFFSET};

        String peakGiftAmtLabel = STRS.getStatsPeakLabel().formatted(pow.getShortPluralName());
        int[] peakGiftAmtLabelPos = {LEFT_START_X, giftAmtLabelPos[1] + LABEL_Y_SPACING};
        String peakGiftAmtStr = this.statsData.peakPowAmts().get("gift") == null
            ? "0"
            : "%,d".formatted(this.statsData.peakPowAmts().get("gift"));
        int[] peakGiftAmtPos = {LEFT_START_X, peakGiftAmtLabelPos[1] + VALUE_Y_OFFSET};

        String giftsUsedLabel = STRS.getStatsUsedLabel().formatted(pow.getShortPluralName());
        int[] giftsUsedLabelPos = {LEFT_START_X, peakGiftAmtLabelPos[1] + LABEL_Y_SPACING};
        String giftsUsedAmtStr = this.statsData.powUsed().get("gift") == null
            ? "0"
            : "%,d".formatted(this.statsData.powUsed().get("gift"));
        int[] giftsUsedAmtPos = {LEFT_START_X, giftsUsedLabelPos[1] + VALUE_Y_OFFSET};

        String handicapLabel = STRS.getStatsGiftHandicapLabel();
        int[] handicapLabelPos = {LEFT_START_X, giftsUsedLabelPos[1] + LABEL_Y_SPACING};

        String handicapPrefix = "<>silver<>";

        if (this.statsData.giftHandicap() > 0) {
            handicapPrefix = "<>error<>+";
        }

        if (this.statsData.giftHandicap() < 0) {
            handicapPrefix = "<>green<>";
        }

        String handicapStr = "%s%,dms".formatted(handicapPrefix, this.statsData.giftHandicap());
        int[] handicapPos = {LEFT_START_X, handicapLabelPos[1] + VALUE_Y_OFFSET};

        String fastestLabel = STRS.getStatsGiftFastestLabel().formatted(pow.getShortName());
        int[] fastestLabelPos = {RIGHT_START_X, START_Y};
        String fastestStr = this.statsData.giftFastest() == NUMS.getInteractiveIdle()
            ? STRS.getUnavailable()
            : "%,dms".formatted(this.statsData.giftFastest());
        int[] fastestPos = {RIGHT_START_X, fastestLabelPos[1] + VALUE_Y_OFFSET};

        String giftsOpenedLabel = STRS.getStatsGiftOpenedLabel().formatted(pow.getShortPluralName());
        int[] giftsOpenedLabelPos = {RIGHT_START_X, fastestLabelPos[1] + LABEL_Y_SPACING};
        String giftsOpenedStr = "%,d".formatted(this.statsData.giftsOpened());
        int[] giftsOpenedPos = {RIGHT_START_X, giftsOpenedLabelPos[1] + VALUE_Y_OFFSET};

        String giftBucksLabel = STRS.getStatsBestBucksLabel();
        int[] giftBucksLabelPos = {RIGHT_START_X, giftsOpenedLabelPos[1] + LABEL_Y_SPACING};
        String giftBucksStr = this.statsData.giftBestBucks() == 0
            ? STRS.getUnavailable()
            : "<>bucks<>$%,d".formatted(this.statsData.giftBestBucks());
        int[] giftBucksPos = {RIGHT_START_X, giftBucksLabelPos[1] + VALUE_Y_OFFSET};

        String giftRarityLabel = STRS.getStatsBestRarityLabel();
        int[] giftRarityLabelPos = {RIGHT_START_X, giftBucksLabelPos[1] + LABEL_Y_SPACING};
        String giftRarityStr = this.statsData.giftBestRarity() == null
            ? STRS.getUnavailable()
            : "<>" + this.statsData.giftBestRarity() + "<>" + RARITIES.get(this.statsData.giftBestRarity()).getName();
        int[] giftRarityPos = {RIGHT_START_X, giftRarityLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, giftAmtLabel, giftAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, giftAmtStr, giftAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, peakGiftAmtLabel, peakGiftAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, peakGiftAmtStr, peakGiftAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, giftsUsedLabel, giftsUsedLabelPos);
        TextUtil.drawValue(this.textDrawer, giftsUsedAmtStr, giftsUsedAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, handicapLabel, handicapLabelPos);
        TextUtil.drawValue(this.textDrawer, handicapStr, handicapPos, true);

        TextUtil.drawLabel(this.textDrawer, fastestLabel, fastestLabelPos);
        TextUtil.drawValue(this.textDrawer, fastestStr, fastestPos, true);

        TextUtil.drawLabel(this.textDrawer, giftsOpenedLabel, giftsOpenedLabelPos);
        TextUtil.drawValue(this.textDrawer, giftsOpenedStr, giftsOpenedPos, true);

        TextUtil.drawLabel(this.textDrawer, giftBucksLabel, giftBucksLabelPos);
        TextUtil.drawValue(this.textDrawer, giftBucksStr, giftBucksPos, true);

        TextUtil.drawLabel(this.textDrawer, giftRarityLabel, giftRarityLabelPos);
        TextUtil.drawValue(this.textDrawer, giftRarityStr, giftRarityPos, true);
    }

    private void drawPageEight() {
        final int LEFT_START_X = 363;
        final int RIGHT_START_X = 1103;
        final int START_Y = 577;
        final int LABEL_Y_SPACING = 145;

        int[] questsCompletedLabelPos = {LEFT_START_X, START_Y};
        String questsCompletedStr = "%,d".formatted(this.statsData.questsCompleted());
        int[] questsCompletedPos = {LEFT_START_X, questsCompletedLabelPos[1] + VALUE_Y_OFFSET};

        int[] fullQuestsCompletedLabelPos = {LEFT_START_X, questsCompletedLabelPos[1] + LABEL_Y_SPACING};
        String fullQuestsCompletedStr = "%,d".formatted(this.statsData.fullQuestsCompleted());
        int[] fullQuestsCompletedPos = {LEFT_START_X, fullQuestsCompletedLabelPos[1] + VALUE_Y_OFFSET};

        int[] fastestQuestLabelPos = {LEFT_START_X, fullQuestsCompletedLabelPos[1] + LABEL_Y_SPACING};
        String fastestQuestStr = this.statsData.fastestFullQuest() > TimeUtil.getOneDayMilli() * 7
            ? STRS.getUnavailable()
            : TimeUtil.getTimeDistance(0, this.statsData.fastestFullQuest(), false).substring(3);
        int[] fastestQuestPos = {LEFT_START_X, fastestQuestLabelPos[1] + VALUE_Y_OFFSET};

        int[] autoClaimLabelPos = {LEFT_START_X, fastestQuestLabelPos[1] + LABEL_Y_SPACING};
        String autoClaimStr = this.statsData.questAutoClaim()
            ? "<>green<>ENABLED"
            : "<>error<>DISABLED";
        int[] autoClaimPos = {LEFT_START_X, autoClaimLabelPos[1] + VALUE_Y_OFFSET};

        int[] easyQuestsLabelPos = {RIGHT_START_X, START_Y};
        String easyQuestsStr = "%,d".formatted(this.statsData.easyQuests());
        int[] easyQuestsPos = {RIGHT_START_X, easyQuestsLabelPos[1] + VALUE_Y_OFFSET};

        int[] mediumQuestsLabelPos = {RIGHT_START_X, easyQuestsLabelPos[1] + LABEL_Y_SPACING};
        String mediumQuestsStr = "%,d".formatted(this.statsData.mediumQuests());
        int[] mediumQuestsPos = {RIGHT_START_X, mediumQuestsLabelPos[1] + VALUE_Y_OFFSET};

        int[] hardQuestsLabelPos = {RIGHT_START_X, mediumQuestsLabelPos[1] + LABEL_Y_SPACING};
        String hardQuestsStr = "%,d".formatted(this.statsData.hardQuests());
        int[] hardQuestsPos = {RIGHT_START_X, hardQuestsLabelPos[1] + VALUE_Y_OFFSET};

        int[] veryHardQuestsLabelPos = {RIGHT_START_X, hardQuestsLabelPos[1] + LABEL_Y_SPACING};
        String veryHardQuestsStr = "%,d".formatted(this.statsData.veryHardQuests());
        int[] veryHardQuestsPos = {RIGHT_START_X, veryHardQuestsLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsQuestsCompletedLabel(), questsCompletedLabelPos);
        TextUtil.drawValue(this.textDrawer, questsCompletedStr, questsCompletedPos, true);

        TextUtil.drawLabel(
            this.textDrawer, STRS.getStatsFullQuestsCompletedLabel(), fullQuestsCompletedLabelPos
        );
        TextUtil.drawValue(this.textDrawer, fullQuestsCompletedStr, fullQuestsCompletedPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsFastestFullQuestLabel(), fastestQuestLabelPos);
        TextUtil.drawValue(this.textDrawer, fastestQuestStr, fastestQuestPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsQuestAutoClaimLabel(), autoClaimLabelPos);
        TextUtil.drawValue(this.textDrawer, autoClaimStr, autoClaimPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsEasyQuestsLabel(), easyQuestsLabelPos);
        TextUtil.drawValue(this.textDrawer, easyQuestsStr, easyQuestsPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsMediumQuestsLabel(), mediumQuestsLabelPos);
        TextUtil.drawValue(this.textDrawer, mediumQuestsStr, mediumQuestsPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsHardQuestsLabel(), hardQuestsLabelPos);
        TextUtil.drawValue(this.textDrawer, hardQuestsStr, hardQuestsPos, true);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsVeryHardQuestsLabel(), veryHardQuestsLabelPos);
        TextUtil.drawValue(this.textDrawer, veryHardQuestsStr, veryHardQuestsPos, true);
    }
}
