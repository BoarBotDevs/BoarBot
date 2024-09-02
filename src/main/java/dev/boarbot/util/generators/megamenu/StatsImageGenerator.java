package dev.boarbot.util.generators.megamenu;

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
        }

        this.drawTopInfo();
        return this;
    }

    private void drawPageOne() {
        final int LEFT_START_X = 335;
        final int RIGHT_START_X = 1075;
        final int START_Y = 374;
        final int LEFT_LABEL_Y_SPACING = 145;
        final int RIGHT_LABEL_Y_SPACING = 174;

        int[] bucksLabelPos = {LEFT_START_X, START_Y};
        String bucksStr = "<>bucks<>$%,d".formatted(this.statsData.bucks());
        int[] bucksPos = {LEFT_START_X, bucksLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakBucksLabelPos = {LEFT_START_X, bucksLabelPos[1] + LEFT_LABEL_Y_SPACING};
        String peakBucksStr = "<>bucks<>$%,d".formatted(this.statsData.highestBucks());
        int[] peakBucksPos = {LEFT_START_X, peakBucksLabelPos[1] + VALUE_Y_OFFSET};

        int[] dailiesLabelPos = {LEFT_START_X, peakBucksLabelPos[1] + LEFT_LABEL_Y_SPACING};
        String dailiesStr = "%,d".formatted(this.statsData.dailies());
        int[] dailiesPos = {LEFT_START_X, dailiesLabelPos[1] + VALUE_Y_OFFSET};

        int[] dailiesMissedLabelPos = {LEFT_START_X, dailiesLabelPos[1] + LEFT_LABEL_Y_SPACING};
        String dailiesMissedStr = "%,d".formatted(this.statsData.dailiesMissed());
        int[] dailiesMissedPos = {LEFT_START_X, dailiesMissedLabelPos[1] + VALUE_Y_OFFSET};

        int[] lastDailyLabelPos = {LEFT_START_X, dailiesMissedLabelPos[1] + LEFT_LABEL_Y_SPACING};
        String lastDailyStr = this.statsData.lastDailyTimestamp() == null
            ? this.strConfig.getUnavailable()
            : Instant.ofEpochMilli(this.statsData.lastDailyTimestamp().getTime())
                .atOffset(ZoneOffset.UTC)
                .format(TimeUtil.getDateFormatter());
        int[] lastDailyPos = {LEFT_START_X, lastDailyLabelPos[1] + VALUE_Y_OFFSET};

        int[] lastBoarLabelPos = {LEFT_START_X, lastDailyLabelPos[1] + LEFT_LABEL_Y_SPACING};
        String lastBoarStr = this.statsData.lastBoar() == null
                ? this.strConfig.getUnavailable()
                : "<>%s<>".formatted(BoarUtil.findRarityKey(this.statsData.lastBoar())) +
                this.itemConfig.getBoars().get(this.statsData.lastBoar()).getName();
        int[] lastBoarPos = {LEFT_START_X, lastBoarLabelPos[1] + VALUE_Y_OFFSET};

        int[] favBoarLabelPos = {LEFT_START_X, lastBoarLabelPos[1] + LEFT_LABEL_Y_SPACING};
        String favBoarStr = this.statsData.favBoar() == null
                ? this.strConfig.getUnavailable()
                : "<>%s<>".formatted(BoarUtil.findRarityKey(this.statsData.favBoar())) +
                this.itemConfig.getBoars().get(this.statsData.favBoar()).getName();
        int[] favBoarPos = {LEFT_START_X, favBoarLabelPos[1] + VALUE_Y_OFFSET};

        int[] totalLabelPos = {RIGHT_START_X, START_Y};
        String totalStr = "%,d".formatted(this.statsData.totalBoars());
        int[] totalPos = {RIGHT_START_X, totalLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakTotalLabelPos = {RIGHT_START_X, totalLabelPos[1] + RIGHT_LABEL_Y_SPACING};
        String peakTotalStr = "%,d".formatted(this.statsData.highestBoars());
        int[] peakTotalPos = {RIGHT_START_X, peakTotalLabelPos[1] + VALUE_Y_OFFSET};

        int[] uniquesLabelPos = {RIGHT_START_X, peakTotalLabelPos[1] + RIGHT_LABEL_Y_SPACING};
        String uniquesStr = "%,d".formatted(this.statsData.uniques());
        int[] uniquesPos = {RIGHT_START_X, uniquesLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakUniquesLabelPos = {RIGHT_START_X, uniquesLabelPos[1] + RIGHT_LABEL_Y_SPACING};
        String peakUniquesStr = "%,d".formatted(this.statsData.highestUniques());
        int[] peakUniquesPos = {RIGHT_START_X, peakUniquesLabelPos[1] + VALUE_Y_OFFSET};

        int[] streakLabelPos = {RIGHT_START_X, peakUniquesLabelPos[1] + RIGHT_LABEL_Y_SPACING};
        String streakStr = "%,d".formatted(this.statsData.boarStreak());
        int[] streakPos = {RIGHT_START_X, streakLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakStreakLabelPos = {RIGHT_START_X, streakLabelPos[1] + RIGHT_LABEL_Y_SPACING};
        String peakStreakStr = "%,d".formatted(this.statsData.highestStreak());
        int[] peakStreakPos = {RIGHT_START_X, peakStreakLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsBucksLabel(), bucksLabelPos);
        TextUtil.drawValue(this.textDrawer, bucksStr, bucksPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakBucksLabel(), peakBucksLabelPos);
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

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsTotalBoarsLabel(), totalLabelPos);
        TextUtil.drawValue(this.textDrawer, totalStr, totalPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakTotalBoarsLabel(), peakTotalLabelPos);
        TextUtil.drawValue(this.textDrawer, peakTotalStr, peakTotalPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsUniquesLabel(), uniquesLabelPos);
        TextUtil.drawValue(this.textDrawer, uniquesStr, uniquesPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakUniquesLabel(), peakUniquesLabelPos);
        TextUtil.drawValue(this.textDrawer, peakUniquesStr, peakUniquesPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsStreakLabel(), streakLabelPos);
        TextUtil.drawValue(this.textDrawer, streakStr, streakPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakStreakLabel(), peakStreakLabelPos);
        TextUtil.drawValue(this.textDrawer, peakStreakStr, peakStreakPos, true);
    }

    private void drawPageTwo() {
        final int LEFT_START_X = 240;
        final int RIGHT_START_X = 980;
        final int START_Y = 519;
        final int LABEL_Y_SPACING = 145;

        int[] blessingsLabelPos = {LEFT_START_X, START_Y};
        String blessHex = TextUtil.getBlessHex(this.statsData.blessings());
        String blessingsStr = "%,d".formatted(this.statsData.blessings());
        int[] blessingsPos = {LEFT_START_X, blessingsLabelPos[1] + VALUE_Y_OFFSET};

        int[] streakBlessLabelPos = {LEFT_START_X, blessingsLabelPos[1] + LABEL_Y_SPACING};
        String streakBlessStr = this.statsData.streakBless() == this.nums.getMaxStreakBless()
            ? "<>gold<>" + this.statsData.streakBless()
            : Integer.toString(this.statsData.streakBless());
        int[] streakBlessPos = {LEFT_START_X, streakBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] questBlessLabelPos = {LEFT_START_X, streakBlessLabelPos[1] + LABEL_Y_SPACING};
        String questBlessStr = this.statsData.questBless() == this.nums.getMaxQuestBless()
            ? "<>gold<>" + this.statsData.questBless()
            : Integer.toString(this.statsData.questBless());
        int[] questBlessPos = {LEFT_START_X, questBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] uniqueBlessLabelPos = {LEFT_START_X, questBlessLabelPos[1] + LABEL_Y_SPACING};
        String uniqueBlessStr = this.statsData.uniqueBless() == this.nums.getMaxUniqueBless()
            ? "<>gold<>" + this.statsData.uniqueBless()
            : Integer.toString(this.statsData.uniqueBless());
        int[] uniqueBlessPos = {LEFT_START_X, uniqueBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] otherBlessLabelPos = {LEFT_START_X, uniqueBlessLabelPos[1] + LABEL_Y_SPACING};
        String otherBlessStr = this.statsData.otherBless() == this.nums.getMaxOtherBless()
            ? "<>gold<>" + this.statsData.otherBless()
            : Integer.toString(this.statsData.otherBless());
        int[] otherBlessPos = {LEFT_START_X, otherBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakBlessingsLabelPos = {RIGHT_START_X, START_Y};
        String peakBlessHex = TextUtil.getBlessHex(this.statsData.highestBlessings());
        String peakBlessingsStr = "%,d".formatted(this.statsData.highestBlessings());
        int[] peakBlessingsPos = {RIGHT_START_X, peakBlessingsLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakStreakBlessLabelPos = {RIGHT_START_X, peakBlessingsLabelPos[1] + LABEL_Y_SPACING};
        String peakStreakBlessStr = this.statsData.highestStreakBless() == this.nums.getMaxStreakBless()
            ? "<>gold<>" + this.statsData.highestStreakBless()
            : Integer.toString(this.statsData.highestStreakBless());
        int[] peakStreakBlessPos = {RIGHT_START_X, peakStreakBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakQuestBlessLabelPos = {RIGHT_START_X, peakStreakBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakQuestBlessStr = this.statsData.highestQuestBless() == this.nums.getMaxQuestBless()
            ? "<>gold<>" + this.statsData.highestQuestBless()
            : Integer.toString(this.statsData.highestQuestBless());
        int[] peakQuestBlessPos = {RIGHT_START_X, peakQuestBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakUniqueBlessLabelPos = {RIGHT_START_X, questBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakUniqueBlessStr = this.statsData.highestUniqueBless() == this.nums.getMaxUniqueBless()
            ? "<>gold<>" + this.statsData.highestUniqueBless()
            : Integer.toString(this.statsData.highestUniqueBless());
        int[] peakUniqueBlessPos = {RIGHT_START_X, peakUniqueBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] peakOtherBlessLabelPos = {RIGHT_START_X, peakUniqueBlessLabelPos[1] + LABEL_Y_SPACING};
        String peakOtherBlessStr = this.statsData.highestOtherBless() == this.nums.getMaxOtherBless()
            ? "<>gold<>" + this.statsData.highestOtherBless()
            : Integer.toString(this.statsData.highestOtherBless());
        int[] peakOtherBlessPos = {RIGHT_START_X, peakOtherBlessLabelPos[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsBlessingsLabel(), blessingsLabelPos);
        TextUtil.drawValue(this.textDrawer, blessingsStr, blessingsPos, true, blessHex);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsStreakBlessLabel(), streakBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, streakBlessStr, streakBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsQuestBlessLabel(), questBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, questBlessStr, questBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsUniqueBlessLabel(), uniqueBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, uniqueBlessStr, uniqueBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsOtherBlessLabel(), otherBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, otherBlessStr, otherBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakBlessingsLabel(), peakBlessingsLabelPos);
        TextUtil.drawValue(this.textDrawer, peakBlessingsStr, peakBlessingsPos, true, peakBlessHex);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakStreakBlessLabel(), peakStreakBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakStreakBlessStr, peakStreakBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakQuestBlessLabel(), peakQuestBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakQuestBlessStr, peakQuestBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakUniqueBlessLabel(), peakUniqueBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakUniqueBlessStr, peakUniqueBlessPos, true);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getStatsPeakOtherBlessLabel(), peakOtherBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, peakOtherBlessStr, peakOtherBlessPos, true);
    }
}
