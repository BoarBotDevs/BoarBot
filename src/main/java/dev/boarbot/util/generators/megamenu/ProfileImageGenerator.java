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
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ProfileImageGenerator extends MegaMenuGenerator {
    private final int maxUniques;

    private static final int[] ORIGIN = {0, 0};
    private static final int[] LEFT_START_POS = {70, 350};
    private static final int VALUE_Y_OFFSET = 78;
    private static final int LABEL_Y_SPACING = 198;
    private static final int LEFT_RIGHT_PADDING = 80;
    private static final int[] BOTTOM_START_POS = {70, 1019};
    private static final int[] RIGHT_START_POS = {1044, 350};
    private static final int[] RIGHT_RIGHT_START_POS = {1473, 548};
    private static final int[] RECENT_LABEL_POS = {1105, 959};
    private static final int[] RECENT_POS = {1025, 984};
    private static final int[] FAVORITE_LABEL_POS = {1509, 959};
    private static final int[] FAVORITE_POS = {1459, 984};

    private final ProfileData profileData;
    private final String favoriteID;

    private TextDrawer textDrawer;

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

        int[] bucksPos = {LEFT_START_POS[0], LEFT_START_POS[1] + VALUE_Y_OFFSET};

        int[] totalLabelPos = {LEFT_START_POS[0], LEFT_START_POS[1] + LABEL_Y_SPACING};
        int[] totalPos = {LEFT_START_POS[0], totalLabelPos[1] + VALUE_Y_OFFSET};

        int[] uniquesLabelPos = {LEFT_START_POS[0], totalLabelPos[1] + LABEL_Y_SPACING};
        int[] uniquesPos = {LEFT_START_POS[0], uniquesLabelPos[1] + VALUE_Y_OFFSET};

        g2d.setFont(BoarBotApp.getBot().getFont().deriveFont((float) mediumFont));
        FontMetrics fm = g2d.getFontMetrics();

        int maxStrLen = Math.max(
            fm.stringWidth(this.strConfig.getProfileUniquesLabel()),
            fm.stringWidth(this.strConfig.getProfileTotalLabel())
        );

        g2d.setFont(BoarBotApp.getBot().getFont().deriveFont((float) bigFont));
        fm = g2d.getFontMetrics();

        String totalBoarStr = "%,d".formatted(this.profileData.totalBoars());
        String uniqueBoarsStr = this.getFractionStr(this.profileData.uniqueBoars(), this.maxUniques);

        maxStrLen = Math.max(maxStrLen, fm.stringWidth(totalBoarStr));
        maxStrLen = Math.max(maxStrLen, fm.stringWidth(uniqueBoarsStr.replaceAll("<>(.*?)<>", "")));

        int[] dailyLabelPos = {totalLabelPos[0] + maxStrLen + LEFT_RIGHT_PADDING, totalLabelPos[1]};
        int[] dailyPos = {dailyLabelPos[0], dailyLabelPos[1] + VALUE_Y_OFFSET};

        int[] streakLabelPos = {dailyLabelPos[0], dailyLabelPos[1] + LABEL_Y_SPACING};
        int[] streakPos = {streakLabelPos[0], streakLabelPos[1] + VALUE_Y_OFFSET};

        int[] nextDailyPos = {BOTTOM_START_POS[0], BOTTOM_START_POS[1] + VALUE_Y_OFFSET};

        String nextDailyStr = this.getDailyStr();

        int[] nextQuestLabelPos = {BOTTOM_START_POS[0], BOTTOM_START_POS[1] + LABEL_Y_SPACING};
        int[] nextQuestPos = {nextQuestLabelPos[0], nextQuestLabelPos[1] + VALUE_Y_OFFSET};

        String nextQuestStr = TimeUtil.getTimeDistance(TimeUtil.getQuestResetMilli(), false);
        nextQuestStr = Character.toUpperCase(nextQuestStr.charAt(0)) + nextQuestStr.substring(1);

        int[] blessPos = {RIGHT_START_POS[0], RIGHT_START_POS[1] + VALUE_Y_OFFSET};

        String blessHex = this.getBlessHex();
        String blessStr = "%,d<>silver<>/<>blessing2<>%,d".formatted(
            this.profileData.blessings(), this.nums.getMaxStreakBless() + this.nums.getMaxQuestBless() +
                this.nums.getMaxUniqueBless() + this.nums.getMaxOtherBless()
        );

        int[] streakBlessLabelPos = {RIGHT_START_POS[0], RIGHT_START_POS[1] + LABEL_Y_SPACING};
        int[] streakBlessPos = {streakBlessLabelPos[0], streakBlessLabelPos[1] + VALUE_Y_OFFSET};

        String streakBlessStr = this.getFractionStr(this.profileData.streakBless(), this.nums.getMaxStreakBless());

        int[] questBlessLabelPos = {streakBlessLabelPos[0], streakBlessLabelPos[1] + LABEL_Y_SPACING};
        int[] questBlessPos = {questBlessLabelPos[0], questBlessLabelPos[1] + VALUE_Y_OFFSET};

        String questBlessStr = this.getFractionStr(this.profileData.questBless(), this.nums.getMaxQuestBless());

        int[] uniqueBlessPos = {RIGHT_RIGHT_START_POS[0], RIGHT_RIGHT_START_POS[1] + VALUE_Y_OFFSET};

        String uniqueBlessStr = this.getFractionStr(this.profileData.uniqueBless(), this.nums.getMaxUniqueBless());

        int[] otherBlessLabelPos = {RIGHT_RIGHT_START_POS[0], RIGHT_RIGHT_START_POS[1] + LABEL_Y_SPACING};
        int[] otherBlessPos = {otherBlessLabelPos[0], otherBlessLabelPos[1] + VALUE_Y_OFFSET};

        String otherBlessStr = this.getFractionStr(this.profileData.otherBless(), this.nums.getMaxOtherBless());

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

        this.textDrawer = new TextDrawer(
            g2d,
            this.strConfig.getProfileBucksLabel(),
            LEFT_START_POS,
            Align.LEFT,
            this.colorConfig.get("font"),
            mediumFont
        );
        textDrawer.drawText();

        this.drawValue("<>bucks<>$%,d".formatted(this.profileData.boarBucks()), bucksPos);

        this.drawLabel(this.strConfig.getProfileTotalLabel(), totalLabelPos);
        this.drawValue(totalBoarStr, totalPos);

        this.drawLabel(this.strConfig.getProfileUniquesLabel(), uniquesLabelPos);
        this.drawValue(uniqueBoarsStr, uniquesPos);

        this.drawLabel(this.strConfig.getProfileDailiesLabel(), dailyLabelPos);
        this.drawValue("%,d".formatted(this.profileData.numDailies()), dailyPos);

        this.drawLabel(this.strConfig.getProfileStreakLabel(), streakLabelPos);
        this.drawValue("%,d".formatted(this.profileData.streak()), streakPos);

        this.drawLabel(this.strConfig.getProfileNextDailyLabel(), BOTTOM_START_POS);
        this.drawValue(nextDailyStr, nextDailyPos);

        this.drawLabel(this.strConfig.getProfileQuestResetLabel(), nextQuestLabelPos);
        this.drawValue(nextQuestStr, nextQuestPos);

        this.drawLabel(this.strConfig.getProfileBlessingsLabel(), RIGHT_START_POS);
        this.drawValue(blessStr, blessPos, blessHex);

        this.drawLabel(this.strConfig.getProfileStreakBlessLabel(), streakBlessLabelPos);
        this.drawValue(streakBlessStr, streakBlessPos);

        this.drawLabel(this.strConfig.getProfileQuestBlessLabel(), questBlessLabelPos);
        this.drawValue(questBlessStr, questBlessPos);

        this.drawLabel(this.strConfig.getProfileUniqueBlessLabel(), RIGHT_RIGHT_START_POS);
        this.drawValue(uniqueBlessStr, uniqueBlessPos);

        this.drawLabel(this.strConfig.getProfileOtherBlessLabel(), otherBlessLabelPos);
        this.drawValue(otherBlessStr, otherBlessPos);

        this.drawLabel(recentStr, RECENT_LABEL_POS);

        if (this.profileData.lastBoarID() != null) {
            BufferedImage boarImage = BoarBotApp.getBot().getImageCacheMap().get(
                "medium" + this.profileData.lastBoarID()
            );
            g2d.drawImage(boarImage, RECENT_POS[0], RECENT_POS[1], null);

            BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("border" + recentRarityKey);
            g2d.drawImage(rarityBorderImage, RECENT_POS[0], RECENT_POS[1], null);
        }

        this.drawLabel(favoriteStr, FAVORITE_LABEL_POS);

        if (this.favoriteID != null) {
            BufferedImage boarImage = BoarBotApp.getBot().getImageCacheMap().get("medium" + this.favoriteID);
            g2d.drawImage(boarImage, FAVORITE_POS[0], FAVORITE_POS[1], null);

            BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("border" + favoriteRarityKey);
            g2d.drawImage(rarityBorderImage, FAVORITE_POS[0], FAVORITE_POS[1], null);
        }

        this.drawTopInfo();
        return this;
    }

    private void drawLabel(String text, int[] pos) {
        this.drawLabel(text, pos, this.colorConfig.get("font"));
    }

    private void drawLabel(String text, int[] pos, String colorVal) {
        int mediumFont = this.nums.getFontMedium();

        this.textDrawer.setText(text);
        this.textDrawer.setPos(pos);
        this.textDrawer.setFontSize(mediumFont);
        this.textDrawer.setColorVal(colorVal);
        this.textDrawer.drawText();
    }

    private void drawValue(String text, int[] pos) {
        this.drawValue(text, pos, this.colorConfig.get("silver"));
    }

    private void drawValue(String text, int[] pos, String colorVal) {
        int bigFont = this.nums.getFontBig();

        this.textDrawer.setText(text);
        this.textDrawer.setPos(pos);
        this.textDrawer.setFontSize(bigFont);
        this.textDrawer.setColorVal(colorVal);
        this.textDrawer.drawText();
    }

    private String getFractionStr(int val, int max) {
        return val >= max
            ? "<>gold<>%,d<>silver<>/<>gold<>%,d".formatted(val, max)
            : "<>error<>%,d<>silver<>/<>green<>%,d".formatted(val, max);
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

    private String getBlessHex() {
        String[] bless1Colors = this.colorConfig.get("blessing1").substring(1).split("(?<=\\G.{2})");
        String[] bless2Colors = this.colorConfig.get("blessing2").substring(1).split("(?<=\\G.{2})");
        double blessPercent = this.profileData.blessings() / (double) (this.nums.getMaxStreakBless() +
            this.nums.getMaxQuestBless() + this.nums.getMaxUniqueBless() + this.nums.getMaxOtherBless());

        String blessRed = Integer.toHexString(
            (int) (Integer.parseInt(bless1Colors[0], 16) + blessPercent *
                (Integer.parseInt(bless2Colors[0], 16) - Integer.parseInt(bless1Colors[0], 16)))
        );
        blessRed = blessRed.length() == 1
            ? "0" + blessRed
            : blessRed;

        String blessGreen = Integer.toHexString(
            (int) (Integer.parseInt(bless1Colors[1], 16) + blessPercent *
                (Integer.parseInt(bless2Colors[1], 16) - Integer.parseInt(bless1Colors[1], 16)))
        );
        blessGreen = blessGreen.length() == 1
            ? "0" + blessGreen
            : blessGreen;

        String blessBlue = Integer.toHexString(
            (int) (Integer.parseInt(bless1Colors[2], 16) + blessPercent *
                (Integer.parseInt(bless2Colors[2], 16) - Integer.parseInt(bless1Colors[2], 16)))
        );
        blessBlue = blessBlue.length() == 1
            ? "0" + blessBlue
            : blessBlue;

        return "#" + blessRed + blessGreen + blessBlue;
    }
}
