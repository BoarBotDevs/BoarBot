package dev.boarbot.util.generators.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.ProfileData;
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
import java.util.List;

public class ProfileImageGenerator extends MegaMenuGenerator {
    private final int maxUniques;

    private static final int[] ORIGIN = {0, 0};
    private static final int LEFT_START_X = 70;
    private static final int LEFT_START_Y = 350;
    private static final int VALUE_Y_OFFSET = 78;
    private static final int LABEL_Y_SPACING = 198;
    private static final int LEFT_RIGHT_PADDING = 80;
    private static final int[] BOTTOM_START_POS = {70, 1019};
    private static final int RIGHT_START_X = 1044;
    private static final int RIGHT_START_Y = 350;
    private static final int RIGHT_RIGHT_START_X = 1473;
    private static final int RIGHT_RIGHT_START_Y = 548;
    private static final int[] RECENT_LABEL_POS = {1105, 959};
    private static final int[] RECENT_POS = {1025, 984};
    private static final int[] FAVORITE_LABEL_POS = {1509, 959};
    private static final int[] FAVORITE_POS = {1459, 984};

    private final ProfileData profileData;
    private final String favoriteID;

    public ProfileImageGenerator(
        int page,
        BoarUser boarUser,
        List<String> badgeIDs,
        String firstJoinedDate,
        String favoriteID,
        boolean isSkyblockGuild,
        ProfileData profileData
    ) {
        super(page, boarUser, badgeIDs, firstJoinedDate);
        this.profileData = profileData;
        this.favoriteID = favoriteID;

        int maxUniques = 0;
        for (String boarID : this.itemConfig.getBoars().keySet()) {
            RarityConfig boarRarity = this.config.getRarityConfigs().get(BoarUtil.findRarityKey(boarID));
            BoarItemConfig boar = this.itemConfig.getBoars().get(boarID);

            boolean countableUnique = !boar.isBlacklisted() && boarRarity.isHunterNeed();
            boolean skyblockBlocked = boar.isSB() && !isSkyblockGuild && this.profileData.numSkyblock() == 0;

            if (countableUnique && !skyblockBlocked) {
                maxUniques++;
            }
        }

        this.maxUniques = maxUniques;
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        int mediumFont = this.nums.getFontMedium();
        int bigFont = this.nums.getFontBig();

        int[] bucksLabelPos = {LEFT_START_X, LEFT_START_Y};
        String bucksStr = "<>bucks<>$%,d".formatted(this.profileData.boarBucks());
        int[] bucksPos = {LEFT_START_X, bucksLabelPos[1] + VALUE_Y_OFFSET};

        int[] totalLabelPos = {LEFT_START_X, bucksLabelPos[1] + LABEL_Y_SPACING};
        String totalBoarStr = "%,d".formatted(this.profileData.totalBoars());
        int[] totalPos = {LEFT_START_X, totalLabelPos[1] + VALUE_Y_OFFSET};

        int[] uniquesLabelPos = {LEFT_START_X, totalLabelPos[1] + LABEL_Y_SPACING};
        String uniqueBoarsStr = TextUtil.getFractionStr(this.profileData.uniqueBoars(), this.maxUniques);
        int[] uniquesPos = {LEFT_START_X, uniquesLabelPos[1] + VALUE_Y_OFFSET};

        g2d.setFont(BoarBotApp.getBot().getFont().deriveFont((float) mediumFont));
        FontMetrics fm = g2d.getFontMetrics();

        int maxStrLen = Math.max(
            fm.stringWidth(this.strConfig.getProfileUniquesLabel()),
            fm.stringWidth(this.strConfig.getProfileTotalLabel())
        );

        g2d.setFont(BoarBotApp.getBot().getFont().deriveFont((float) bigFont));
        fm = g2d.getFontMetrics();

        maxStrLen = Math.max(maxStrLen, fm.stringWidth(totalBoarStr));
        maxStrLen = Math.max(maxStrLen, fm.stringWidth(uniqueBoarsStr.replaceAll("<>(.*?)<>", "")));

        int[] dailyLabelPos = {totalLabelPos[0] + maxStrLen + LEFT_RIGHT_PADDING, totalLabelPos[1]};
        String dailyStr = "%,d".formatted(this.profileData.numDailies());
        int[] dailyPos = {dailyLabelPos[0], dailyLabelPos[1] + VALUE_Y_OFFSET};

        int[] streakLabelPos = {dailyLabelPos[0], dailyLabelPos[1] + LABEL_Y_SPACING};
        String streakStr = "%,d".formatted(this.profileData.streak());
        int[] streakPos = {streakLabelPos[0], streakLabelPos[1] + VALUE_Y_OFFSET};

        int[] nextDailyLabelPos = BOTTOM_START_POS;
        String nextDailyStr = this.getDailyStr();
        int[] nextDailyPos = {nextDailyLabelPos[0], nextDailyLabelPos[1] + VALUE_Y_OFFSET};

        int[] nextQuestLabelPos = {BOTTOM_START_POS[0], BOTTOM_START_POS[1] + LABEL_Y_SPACING};
        String nextQuestStr = TimeUtil.getTimeDistance(TimeUtil.getQuestResetMilli(), false);
        nextQuestStr = Character.toUpperCase(nextQuestStr.charAt(0)) + nextQuestStr.substring(1);
        int[] nextQuestPos = {nextQuestLabelPos[0], nextQuestLabelPos[1] + VALUE_Y_OFFSET};

        int[] blessLabelPos = {RIGHT_START_X, RIGHT_START_Y};

        String blessHex = TextUtil.getBlessHex(this.profileData.blessings());
        String blessStr = "%,d<>silver<>/<>blessing2<>%,d".formatted(
            this.profileData.blessings(), this.nums.getMaxStreakBless() + this.nums.getMaxQuestBless() +
                this.nums.getMaxUniqueBless() + this.nums.getMaxOtherBless()
        );

        if (this.profileData.blessings() > 1000) {
            blessStr = this.strConfig.getBlessingsSymbol() + " " + blessStr;
        }

        int[] blessPos = {RIGHT_START_X, blessLabelPos[1] + VALUE_Y_OFFSET};

        int[] streakBlessLabelPos = {RIGHT_START_X, blessLabelPos[1] + LABEL_Y_SPACING};
        String streakBlessStr = TextUtil.getFractionStr(this.profileData.streakBless(), this.nums.getMaxStreakBless());
        int[] streakBlessPos = {RIGHT_START_X, streakBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] questBlessLabelPos = {RIGHT_START_X, streakBlessLabelPos[1] + LABEL_Y_SPACING};
        String questBlessStr = TextUtil.getFractionStr(this.profileData.questBless(), this.nums.getMaxQuestBless());
        int[] questBlessPos = {RIGHT_START_X, questBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] uniqueBlessLabelPos = {RIGHT_RIGHT_START_X, RIGHT_RIGHT_START_Y};
        String uniqueBlessStr = TextUtil.getFractionStr(this.profileData.uniqueBless(), this.nums.getMaxUniqueBless());
        int[] uniqueBlessPos = {RIGHT_START_X, uniqueBlessLabelPos[1] + VALUE_Y_OFFSET};

        int[] otherBlessLabelPos = {RIGHT_RIGHT_START_X, uniqueBlessLabelPos[1] + LABEL_Y_SPACING};
        String otherBlessStr = TextUtil.getFractionStr(this.profileData.otherBless(), this.nums.getMaxOtherBless());
        int[] otherBlessPos = {otherBlessLabelPos[0], otherBlessLabelPos[1] + VALUE_Y_OFFSET};

        String recentRarityKey = this.profileData.lastBoarID() == null
            ? ""
            : BoarUtil.findRarityKey(this.profileData.lastBoarID());
        String recentStr = this.profileData.lastBoarID() == null
            ? this.strConfig.getProfileRecentLabel()
            : "<>%s<>%s".formatted(recentRarityKey, this.strConfig.getProfileRecentLabel());

        String favoriteRarityKey = this.favoriteID == null
            ? ""
            : BoarUtil.findRarityKey(this.favoriteID);
        String favoriteStr = this.favoriteID == null
            ? this.strConfig.getProfileFavLabel()
            : "<>%s<>%s".formatted(favoriteRarityKey, this.strConfig.getProfileFavLabel());

        String underlayPath = this.pathConfig.getMegaMenuAssets() + this.pathConfig.getProfUnderlay();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN, IMAGE_SIZE);

        this.textDrawer = new TextDrawer(g2d, "", ORIGIN, Align.LEFT, this.colorConfig.get("font"), mediumFont);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileBucksLabel(), bucksLabelPos);
        TextUtil.drawValue(this.textDrawer, bucksStr, bucksPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileTotalLabel(), totalLabelPos);
        TextUtil.drawValue(this.textDrawer, totalBoarStr, totalPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileUniquesLabel(), uniquesLabelPos);
        TextUtil.drawValue(this.textDrawer, uniqueBoarsStr, uniquesPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileDailiesLabel(), dailyLabelPos);
        TextUtil.drawValue(this.textDrawer, dailyStr, dailyPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileStreakLabel(), streakLabelPos);
        TextUtil.drawValue(this.textDrawer, streakStr, streakPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileNextDailyLabel(), BOTTOM_START_POS);
        TextUtil.drawValue(this.textDrawer, nextDailyStr, nextDailyPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileQuestResetLabel(), nextQuestLabelPos);
        TextUtil.drawValue(this.textDrawer, nextQuestStr, nextQuestPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileBlessingsLabel(), blessLabelPos);
        TextUtil.drawValue(this.textDrawer, blessStr, blessPos, false, blessHex);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileStreakBlessLabel(), streakBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, streakBlessStr, streakBlessPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileQuestBlessLabel(), questBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, questBlessStr, questBlessPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileUniqueBlessLabel(), uniqueBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, uniqueBlessStr, uniqueBlessPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getProfileOtherBlessLabel(), otherBlessLabelPos);
        TextUtil.drawValue(this.textDrawer, otherBlessStr, otherBlessPos);

        TextUtil.drawLabel(this.textDrawer, recentStr, RECENT_LABEL_POS);

        if (this.profileData.lastBoarID() != null) {
            BufferedImage boarImage = BoarBotApp.getBot().getImageCacheMap().get(
                "medium" + this.profileData.lastBoarID()
            );
            g2d.drawImage(boarImage, RECENT_POS[0], RECENT_POS[1], null);

            BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("border" + recentRarityKey);
            g2d.drawImage(rarityBorderImage, RECENT_POS[0], RECENT_POS[1], null);
        }

        TextUtil.drawLabel(this.textDrawer, favoriteStr, FAVORITE_LABEL_POS);

        if (this.favoriteID != null) {
            BufferedImage boarImage = BoarBotApp.getBot().getImageCacheMap().get("medium" + this.favoriteID);
            g2d.drawImage(boarImage, FAVORITE_POS[0], FAVORITE_POS[1], null);

            BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("border" + favoriteRarityKey);
            g2d.drawImage(rarityBorderImage, FAVORITE_POS[0], FAVORITE_POS[1], null);
        }

        this.drawTopInfo();
        return this;
    }

    private String getDailyStr() {
        boolean dailyReady = this.profileData.lastDailyTimestamp() == null ||
            this.profileData.lastDailyTimestamp().getTime() < TimeUtil.getLastDailyResetMilli();
        long milliDistance = TimeUtil.getNextDailyResetMilli() - TimeUtil.getCurMilli();
        boolean showTime = this.profileData.lastDailyTimestamp() != null &&
            this.profileData.lastDailyTimestamp().getTime() >= TimeUtil.getLastDailyResetMilli() - 1000 * 60 * 60 * 24;
        String distanceColor = milliDistance <= 1000 * 60 * 60 * 6
            ? "<>error<>"
            : "<>maintenance<>";

        String nextDailyStr;

        if (dailyReady) {
            nextDailyStr = "<>green<>" + this.strConfig.getProfileDailyReady();
            nextDailyStr += showTime
                ? " <>silver<>(%s%s<>silver<>)".formatted(
                    distanceColor, TimeUtil.getTimeDistance(TimeUtil.getNextDailyResetMilli(), true)
                )
                : "";
        } else {
            nextDailyStr = TimeUtil.getTimeDistance(TimeUtil.getNextDailyResetMilli(), false);
            nextDailyStr = Character.toUpperCase(nextDailyStr.charAt(0)) + nextDailyStr.substring(1);
        }

        return nextDailyStr;
    }
}
