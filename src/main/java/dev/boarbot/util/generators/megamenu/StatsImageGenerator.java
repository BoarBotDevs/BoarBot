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
    private static final int LEFT_START_X = 336;
    private static final int RIGHT_START_X = 1076;
    private static final int START_Y = 374;
    private static final int VALUE_Y_OFFSET = 65;
    private static final int LEFT_LABEL_Y_SPACING = 145;
    private static final int RIGHT_LABEL_Y_SPACING = 174;

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

        int mediumFont = this.nums.getFontMedium();

        int[] bucksLabelPos = {LEFT_START_X, START_Y};
        String bucksStr = "<>bucks<>$%,d".formatted(this.statsData.bucks());
        int[] bucksPos = {bucksLabelPos[0], bucksLabelPos[1] + VALUE_Y_OFFSET};

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
s
        int[] peakStreakLabelPos = {RIGHT_START_X, streakLabelPos[1] + RIGHT_LABEL_Y_SPACING};
        String peakStreakStr = "%,d".formatted(this.statsData.highestStreak());
        int[] peakStreakPos = {RIGHT_START_X, peakStreakLabelPos[1] + VALUE_Y_OFFSET};

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN, IMAGE_SIZE);

        this.textDrawer = new TextDrawer(g2d, "", ORIGIN, Align.LEFT, this.colorConfig.get("font"), mediumFont);

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

        this.drawTopInfo();
        return this;
    }
}
