package dev.boarbot.util.generators.megamenu;

import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.StatsData;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class StatsImageGenerator extends MegaMenuGenerator {
    private static final int[] ORIGIN = {0, 0};
    private static final int VALUE_Y_OFFSET = 65;

    private final StatsData statsData;

    public StatsImageGenerator(
        int page,
        BoarUser boarUser,
        List<String> badgeIDs,
        String firstJoinedDate,
        StatsData statsData
    ) {
        super(page, boarUser, badgeIDs, firstJoinedDate);
        this.statsData = statsData;
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        String underlayPath = this.pathConfig.getMegaMenuAssets() + this.pathConfig.getMegaMenuBase();

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN, IMAGE_SIZE);

        this.textDrawer = new TextDrawer(
            g2d, "", ORIGIN, Align.LEFT, this.colorConfig.get("font"), this.nums.getFontMedium()
        );

        switch (this.page) {
            case 0 -> this.drawPageOne();
            case 1 -> this.drawPageTwo();
            case 2 -> this.drawPageThree();
            case 3 -> this.drawPageFour();
            case 4 -> this.drawPageFive();
        }

        this.drawTopInfo();
        return this;
    }

    private void drawPageOne() {
        final int LEFT_START_X = 335;
        final int RIGHT_START_X = 1075;
        final int START_Y = 354;
        final int LABEL_Y_SPACING = 145;

        int[] bucksLabelPos = {LEFT_START_X, START_Y};
        String bucksStr = "<>bucks<>$%,d".formatted(this.statsData.bucks());
        int[] bucksPos = {LEFT_START_X, bucksLabelPos[1] + VALUE_Y_OFFSET};

        String peakBucksLabel = this.strConfig.getStatsPeakLabel().formatted(this.strConfig.getBucksPluralName());
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
            ? this.strConfig.getUnavailable()
            : Instant.ofEpochMilli(this.statsData.lastDailyTimestamp().getTime())
                .atOffset(ZoneOffset.UTC)
                .format(TimeUtil.getDateFormatter());
        int[] lastDailyPos = {LEFT_START_X, lastDailyLabelPos[1] + VALUE_Y_OFFSET};

        int[] lastBoarLabelPos = {LEFT_START_X, lastDailyLabelPos[1] + LABEL_Y_SPACING};
        String lastBoarStr = this.statsData.lastBoar() == null
            ? this.strConfig.getUnavailable()
            : "<>%s<>".formatted(BoarUtil.findRarityKey(this.statsData.lastBoar())) +
                this.itemConfig.getBoars().get(this.statsData.lastBoar()).getName();
        int[] lastBoarPos = {LEFT_START_X, lastBoarLabelPos[1] + VALUE_Y_OFFSET};

        int[] favBoarLabelPos = {LEFT_START_X, lastBoarLabelPos[1] + LABEL_Y_SPACING};
        String favBoarStr = this.statsData.favBoar() == null
            ? this.strConfig.getUnavailable()
            : "<>%s<>".formatted(BoarUtil.findRarityKey(this.statsData.favBoar())) +
                this.itemConfig.getBoars().get(this.statsData.favBoar()).getName();
        int[] favBoarPos = {LEFT_START_X, favBoarLabelPos[1] + VALUE_Y_OFFSET};

        String totalLabel = this.strConfig.getStatsTotalLabel().formatted(this.strConfig.getMainItemPluralName());
        int[] totalLabelPos = {RIGHT_START_X, START_Y};
        String totalStr = "%,d".formatted(this.statsData.totalBoars());
        int[] totalPos = {RIGHT_START_X, totalLabelPos[1] + VALUE_Y_OFFSET};

        String peakTotalLabel = this.strConfig.getStatsPeakLabel().formatted(totalLabel);
        int[] peakTotalLabelPos = {RIGHT_START_X, totalLabelPos[1] + LABEL_Y_SPACING};
        String peakTotalStr = "%,d".formatted(this.statsData.highestBoars());
        int[] peakTotalPos = {RIGHT_START_X, peakTotalLabelPos[1] + VALUE_Y_OFFSET};

        int[] uniquesLabelPos = {RIGHT_START_X, peakTotalLabelPos[1] + LABEL_Y_SPACING};
        String uniquesStr = "%,d".formatted(this.statsData.uniques());
        int[] uniquesPos = {RIGHT_START_X, uniquesLabelPos[1] + VALUE_Y_OFFSET};

        String peakUniquesLabel = this.strConfig.getStatsPeakLabel().formatted(this.strConfig.getStatsUniquesLabel());
        int[] peakUniquesLabelPos = {RIGHT_START_X, uniquesLabelPos[1] + LABEL_Y_SPACING};
        String peakUniquesStr = "%,d".formatted(this.statsData.highestUniques());
        int[] peakUniquesPos = {RIGHT_START_X, peakUniquesLabelPos[1] + VALUE_Y_OFFSET};

        int[] streakLabelPos = {RIGHT_START_X, peakUniquesLabelPos[1] + LABEL_Y_SPACING};
        String streakStr = "%,d".formatted(this.statsData.boarStreak());
        int[] streakPos = {RIGHT_START_X, streakLabelPos[1] + VALUE_Y_OFFSET};

        String peakStreakLabel = this.strConfig.getStatsPeakLabel().formatted(this.strConfig.getStatsStreakLabel());
        int[] peakStreakLabelPos = {RIGHT_START_X, streakLabelPos[1] + LABEL_Y_SPACING};
        String peakStreakStr = "%,d".formatted(this.statsData.highestStreak());
        int[] peakStreakPos = {RIGHT_START_X, peakStreakLabelPos[1] + VALUE_Y_OFFSET};

        int[] notificationLabelPos = {RIGHT_START_X, peakStreakLabelPos[1] + LABEL_Y_SPACING};
        String notificationStr = this.statsData.notificationsOn()
            ? "<>green<>ENABLED"
            : "<>error<>DISABLED";
        int[] notificationPos = {RIGHT_START_X, notificationLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getBucksPluralName(), bucksLabelPos);
        TextUtil.drawValue(this.textDrawer, bucksStr, bucksPos, true);

        TextUtil.drawLabel(this.textDrawer, peakBucksLabel, peakBucksLabelPos);
        TextUtil.drawValue(this.textDrawer, peakBucksStr, peakBucksPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsDailiesLabel(), dailiesLabelPos);
        TextUtil.drawValue(this.textDrawer, dailiesStr, dailiesPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsDailiesMissedLabel(), dailiesMissedLabelPos);
        TextUtil.drawValue(this.textDrawer, dailiesMissedStr, dailiesMissedPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsLastDailyLabel(), lastDailyLabelPos);
        TextUtil.drawValue(this.textDrawer, lastDailyStr, lastDailyPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsLastBoarLabel(), lastBoarLabelPos);
        TextUtil.drawValue(this.textDrawer, lastBoarStr, lastBoarPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsFavBoarLabel(), favBoarLabelPos);
        TextUtil.drawValue(this.textDrawer, favBoarStr, favBoarPos, true);

        TextUtil.drawLabel(this.textDrawer, totalLabel, totalLabelPos);
        TextUtil.drawValue(this.textDrawer, totalStr, totalPos, true);

        TextUtil.drawLabel(this.textDrawer, peakTotalLabel, peakTotalLabelPos);
        TextUtil.drawValue(this.textDrawer, peakTotalStr, peakTotalPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsUniquesLabel(), uniquesLabelPos);
        TextUtil.drawValue(this.textDrawer, uniquesStr, uniquesPos, true);

        TextUtil.drawLabel(this.textDrawer, peakUniquesLabel, peakUniquesLabelPos);
        TextUtil.drawValue(this.textDrawer, peakUniquesStr, peakUniquesPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsStreakLabel(), streakLabelPos);
        TextUtil.drawValue(this.textDrawer, streakStr, streakPos, true);

        TextUtil.drawLabel(this.textDrawer, peakStreakLabel, peakStreakLabelPos);
        TextUtil.drawValue(this.textDrawer, peakStreakStr, peakStreakPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsNotificationLabel(), notificationLabelPos);
        TextUtil.drawValue(this.textDrawer, notificationStr, notificationPos, true);
    }

    private void drawPageTwo() {
        final int LEFT_START_X = 240;
        final int RIGHT_START_X = 980;
        final int START_Y = 499;
        final int LABEL_Y_SPACING = 145;

        int[] blessingsLabelPos = {LEFT_START_X, START_Y};
        String blessHex = TextUtil.getBlessHex(this.statsData.blessings());
        String blessingsStr = this.statsData.blessings() > 1000
            ? "%s %,d".formatted(this.strConfig.getBlessingsSymbol(), this.statsData.blessings())
            : "%,d".formatted(this.statsData.blessings());
        int[] blessingsPos = {LEFT_START_X, blessingsLabelPos[1] + VALUE_Y_OFFSET};

        String streakBlessLabel = this.strConfig.getStatsBlessLabel().formatted(
            this.strConfig.getBlessingsPluralNameShortened(), this.strConfig.getBlessCategory1()
        );
        int[] streakBlessLabelPos = {LEFT_START_X, blessingsLabelPos[1] + LABEL_Y_SPACING};
        String streakBlessStr = this.statsData.streakBless() == this.nums.getMaxStreakBless()
            ? "<>gold<>" + this.statsData.streakBless()
            : Integer.toString(this.statsData.streakBless());
        int[] streakBlessPos = {LEFT_START_X, streakBlessLabelPos[1] + VALUE_Y_OFFSET};

        String questBlessLabel = this.strConfig.getStatsBlessLabel().formatted(
            this.strConfig.getBlessingsPluralNameShortened(), this.strConfig.getBlessCategory2()
        );
        int[] questBlessLabelPos = {LEFT_START_X, streakBlessLabelPos[1] + LABEL_Y_SPACING};
        String questBlessStr = this.statsData.questBless() == this.nums.getMaxQuestBless()
            ? "<>gold<>" + this.statsData.questBless()
            : Integer.toString(this.statsData.questBless());
        int[] questBlessPos = {LEFT_START_X, questBlessLabelPos[1] + VALUE_Y_OFFSET};

        String uniqueBlessLabel = this.strConfig.getStatsBlessLabel().formatted(
            this.strConfig.getBlessingsPluralNameShortened(), this.strConfig.getBlessCategory3()
        );
        int[] uniqueBlessLabelPos = {LEFT_START_X, questBlessLabelPos[1] + LABEL_Y_SPACING};
        String uniqueBlessStr = this.statsData.uniqueBless() == this.nums.getMaxUniqueBless()
            ? "<>gold<>" + this.statsData.uniqueBless()
            : Integer.toString(this.statsData.uniqueBless());
        int[] uniqueBlessPos = {LEFT_START_X, uniqueBlessLabelPos[1] + VALUE_Y_OFFSET};

        String otherBlessLabel = this.strConfig.getStatsBlessLabel().formatted(
            this.strConfig.getBlessingsPluralNameShortened(), this.strConfig.getBlessCategory4()
        );
        int[] otherBlessLabelPos = {LEFT_START_X, uniqueBlessLabelPos[1] + LABEL_Y_SPACING};
        String otherBlessStr = this.statsData.otherBless() == this.nums.getMaxOtherBless()
            ? "<>gold<>" + this.statsData.otherBless()
            : Integer.toString(this.statsData.otherBless());
        int[] otherBlessPos = {LEFT_START_X, otherBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakBlessingsLabel = this.strConfig.getStatsPeakLabel().formatted(
            this.strConfig.getBlessingsPluralName()
        );
        int[] peakBlessingsLabelPos = {RIGHT_START_X, START_Y};
        String peakBlessHex = TextUtil.getBlessHex(this.statsData.highestBlessings());
        String peakBlessingsStr = this.statsData.highestBlessings() > 1000
            ? "%s %,d".formatted(this.strConfig.getBlessingsSymbol(), this.statsData.highestBlessings())
            : "%,d".formatted(this.statsData.highestBlessings());
        int[] peakBlessingsPos = {RIGHT_START_X, peakBlessingsLabelPos[1] + VALUE_Y_OFFSET};

        String peakStreakBlessLabel = this.strConfig.getStatsPeakLabel().formatted(streakBlessLabel);
        int[] peakStreakBlessLabelPos = {RIGHT_START_X, peakBlessingsLabelPos[1] + LABEL_Y_SPACING};
        String peakStreakBlessStr = this.statsData.highestStreakBless() == this.nums.getMaxStreakBless()
            ? "<>gold<>" + this.statsData.highestStreakBless()
            : Integer.toString(this.statsData.highestStreakBless());
        int[] peakStreakBlessPos = {RIGHT_START_X, peakStreakBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakQuestBlessLabel = this.strConfig.getStatsPeakLabel().formatted(questBlessLabel);
        int[] peakQuestBlessLabelPos = {RIGHT_START_X, peakStreakBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakQuestBlessStr = this.statsData.highestQuestBless() == this.nums.getMaxQuestBless()
            ? "<>gold<>" + this.statsData.highestQuestBless()
            : Integer.toString(this.statsData.highestQuestBless());
        int[] peakQuestBlessPos = {RIGHT_START_X, peakQuestBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakUniqueBlessLabel = this.strConfig.getStatsPeakLabel().formatted(uniqueBlessLabel);
        int[] peakUniqueBlessLabelPos = {RIGHT_START_X, questBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakUniqueBlessStr = this.statsData.highestUniqueBless() == this.nums.getMaxUniqueBless()
            ? "<>gold<>" + this.statsData.highestUniqueBless()
            : Integer.toString(this.statsData.highestUniqueBless());
        int[] peakUniqueBlessPos = {RIGHT_START_X, peakUniqueBlessLabelPos[1] + VALUE_Y_OFFSET};

        String peakOtherBlessLabel = this.strConfig.getStatsPeakLabel().formatted(otherBlessLabel);
        int[] peakOtherBlessLabelPos = {RIGHT_START_X, peakUniqueBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakOtherBlessStr = this.statsData.highestOtherBless() == this.nums.getMaxOtherBless()
            ? "<>gold<>" + this.statsData.highestOtherBless()
            : Integer.toString(this.statsData.highestOtherBless());
        int[] peakOtherBlessPos = {RIGHT_START_X, peakOtherBlessLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getBlessingsPluralName(), blessingsLabelPos);
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
        final int START_Y = 572;
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
        String powFastStr = this.statsData.fastestPowerup() == 0
            ? this.strConfig.getUnavailable()
            : "%,dms".formatted(this.statsData.fastestPowerup());
        int[] powFastPos = {LEFT_START_X, powFastLabelPos[1] + VALUE_Y_OFFSET};

        int[] powAvgLabelPos = {RIGHT_START_X, START_Y};
        String powAvgStr = this.statsData.avgPowerupPlacement() == 0
            ? this.strConfig.getUnavailable()
            : "Top %,.2f%%".formatted(this.statsData.avgPowerupPlacement());
        int[] powAvgPos = {RIGHT_START_X, powAvgLabelPos[1] + VALUE_Y_OFFSET};

        int[] powBestOneLabelPos = {RIGHT_START_X, powAvgLabelPos[1] + LABEL_Y_SPACING};
        String powBestOneStr = this.statsData.bestPrompts().isEmpty()
            ? this.strConfig.getUnavailable()
            : BoarUtil.getPromptStr(this.statsData.bestPrompts().getFirst());
        int[] powBestOnePos = {RIGHT_START_X, powBestOneLabelPos[1] + VALUE_Y_OFFSET};

        int[] powBestTwoLabelPos = {RIGHT_START_X, powBestOneLabelPos[1] + LABEL_Y_SPACING};
        String powBestTwoStr = this.statsData.bestPrompts().size() < 2
            ? this.strConfig.getUnavailable()
            : BoarUtil.getPromptStr(this.statsData.bestPrompts().get(1));
        int[] powBestTwoPos = {RIGHT_START_X, powBestTwoLabelPos[1] + VALUE_Y_OFFSET};

        int[] powBestThreeLabelPos = {RIGHT_START_X, powBestTwoLabelPos[1] + LABEL_Y_SPACING};
        String powBestThreeStr = this.statsData.bestPrompts().size() < 3
            ? this.strConfig.getUnavailable()
            : BoarUtil.getPromptStr(this.statsData.bestPrompts().get(2));
        int[] powBestThreePos = {RIGHT_START_X, powBestThreeLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowAttemptsLabel(), powAttemptsLabelPos);
        TextUtil.drawValue(this.textDrawer, powAttemptsStr, powAttemptsPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowWinsLabel(), powWinsLabelPos);
        TextUtil.drawValue(this.textDrawer, powWinsStr, powWinsPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowPerfectLabel(), powPerfectLabelPos);
        TextUtil.drawValue(this.textDrawer, powPerfectStr, powPerfectPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowFastestLabel(), powFastLabelPos);
        TextUtil.drawValue(this.textDrawer, powFastStr, powFastPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowAvgLabel(), powAvgLabelPos);
        TextUtil.drawValue(this.textDrawer, powAvgStr, powAvgPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowBestLabel1(), powBestOneLabelPos);
        TextUtil.drawValue(this.textDrawer, powBestOneStr, powBestOnePos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowBestLabel2(), powBestTwoLabelPos);
        TextUtil.drawValue(this.textDrawer, powBestTwoStr, powBestTwoPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPowBestLabel3(), powBestThreeLabelPos);
        TextUtil.drawValue(this.textDrawer, powBestThreeStr, powBestThreePos, true);
    }

    private void drawPageFour() {
        final int LEFT_START_X = 315;
        final int RIGHT_START_X = 1055;
        final int START_Y = 572;
        final int LABEL_Y_SPACING = 145;

        PowerupItemConfig pow = this.itemConfig.getPowerups().get("miracle");

        String miracleAmtLabel = this.strConfig.getStatsTotalLabel().formatted(pow.getPluralName());
        int[] miracleAmtLabelPos = {LEFT_START_X, START_Y};
        String miracleAmtStr = "%,d".formatted(this.statsData.powAmts().get("miracle"));
        int[] miracleAmtPos = {LEFT_START_X, miracleAmtLabelPos[1] + VALUE_Y_OFFSET};

        String peakMiracleAmtLabel = this.strConfig.getStatsPeakLabel().formatted(miracleAmtLabel);
        int[] peakMiracleAmtLabelPos = {LEFT_START_X, miracleAmtLabelPos[1] + LABEL_Y_SPACING};
        String peakMiracleAmtStr = "%,d".formatted(this.statsData.peakPowAmts().get("miracle"));
        int[] peakMiracleAmtPos = {LEFT_START_X, peakMiracleAmtLabelPos[1] + VALUE_Y_OFFSET};

        int[] miraclesActiveLabelPos = {LEFT_START_X, peakMiracleAmtLabelPos[1] + LABEL_Y_SPACING};
        String miraclesActiveStr = "%,d".formatted(this.statsData.miraclesActive());
        int[] miraclesActivePos = {LEFT_START_X, miraclesActiveLabelPos[1] + VALUE_Y_OFFSET};

        String miraclesUsedLabel = this.strConfig.getStatsUsedLabel().formatted(pow.getPluralName());
        int[] miraclesUsedLabelPos = {LEFT_START_X, miraclesActiveLabelPos[1] + LABEL_Y_SPACING};
        String miraclesUsedStr = "%,d".formatted(this.statsData.powUsed().get("miracle"));
        int[] miraclesUsedPos = {LEFT_START_X, miraclesUsedLabelPos[1] + VALUE_Y_OFFSET};

        int[] miracleRollsLabelPos = {RIGHT_START_X, START_Y};
        String miracleRollsStr = "%,d".formatted(this.statsData.miracleRolls());
        int[] miracleRollsPos = {RIGHT_START_X, miracleRollsLabelPos[1] + VALUE_Y_OFFSET};

        int[] biggestRollLabelPos = {RIGHT_START_X, miracleRollsLabelPos[1] + LABEL_Y_SPACING};
        String biggestRollStr = this.statsData.miraclesMostUsed() == 0
            ? this.strConfig.getUnavailable()
            : "%,d".formatted(this.statsData.miraclesMostUsed());
        int[] biggestRollPos = {RIGHT_START_X, biggestRollLabelPos[1] + VALUE_Y_OFFSET};

        int[] bestBucksLabelPos = {RIGHT_START_X, biggestRollLabelPos[1] + LABEL_Y_SPACING};
        String bestBucksStr = this.statsData.miracleBestBucks() == 0
            ? this.strConfig.getUnavailable()
            : "<>bucks<>$%,d".formatted(this.statsData.miracleBestBucks());
        int[] bestBucksPos = {RIGHT_START_X, bestBucksLabelPos[1] + VALUE_Y_OFFSET};

        int[] bestRarityLabelPos = {RIGHT_START_X, bestBucksLabelPos[1] + LABEL_Y_SPACING};
        String bestRarityStr = this.statsData.miracleBestRarity() == null
            ? this.strConfig.getUnavailable()
            : "<>" + this.statsData.miracleBestRarity() + "<>" +
                this.config.getRarityConfigs().get(this.statsData.miracleBestRarity()).getName();
        int[] bestRarityPos = {RIGHT_START_X, bestRarityLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, miracleAmtLabel, miracleAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, miracleAmtStr, miracleAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, peakMiracleAmtLabel, peakMiracleAmtLabelPos);
        TextUtil.drawValue(this.textDrawer, peakMiracleAmtStr, peakMiracleAmtPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsMiraclesActiveLabel(), miraclesActiveLabelPos);
        TextUtil.drawValue(this.textDrawer, miraclesActiveStr, miraclesActivePos, true);

        TextUtil.drawLabel(this.textDrawer, miraclesUsedLabel, miraclesUsedLabelPos);
        TextUtil.drawValue(this.textDrawer, miraclesUsedStr, miraclesUsedPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsMiracleRollsLabel(), miracleRollsLabelPos);
        TextUtil.drawValue(this.textDrawer, miracleRollsStr, miracleRollsPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakMiracleRollLabel(), biggestRollLabelPos);
        TextUtil.drawValue(this.textDrawer, biggestRollStr, biggestRollPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsMiracleBucksLabel(), bestBucksLabelPos);
        TextUtil.drawValue(this.textDrawer, bestBucksStr, bestBucksPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsMiracleRarityLabel(), bestRarityLabelPos);
        TextUtil.drawValue(this.textDrawer, bestRarityStr, bestRarityPos, true);
    }

    private void drawPageFive() {
        final int LEFT_START_X = 315;
        final int RIGHT_START_X = 1055;
        final int START_Y = 572;
        final int LABEL_Y_SPACING = 145;

        int[] transmuteAmtLabelPos = {LEFT_START_X, START_Y};
        String transmuteAmtStr = "%,d".formatted(this.statsData.powAmts().get("transmute"));
        int[] transmuteAmtPos = {LEFT_START_X, transmuteAmtLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakTransmuteAmtLabelPos = {LEFT_START_X, transmuteAmtLabelPos[1] + LABEL_Y_SPACING};
        String peakTransmuteAmtStr = "%,d".formatted(this.statsData.peakPowAmts().get("transmute"));
        int[] peakTransmuteAmtPos = {LEFT_START_X, peakTransmuteAmtLabelPos[1] + VALUE_Y_OFFSET};
    }
}
