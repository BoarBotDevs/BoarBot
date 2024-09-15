package dev.boarbot.util.generators.megamenu;

import dev.boarbot.bot.config.quests.IndivQuestConfig;
import dev.boarbot.bot.config.quests.QuestConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.entities.boaruser.data.QuestData;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.quests.QuestUtil;
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class QuestsImageGenerator extends MegaMenuGenerator {
    private static final int QUEST_WIDTH = 815;
    private static final int QUEST_X = 57;
    private static final int QUEST_START_Y = 305;
    private static final int QUEST_VALUE_Y_OFFSET = 78;
    private static final int QUEST_Y_SPACING = 162;
    private static final int BUCKS_X = 1042;
    private static final int BUCKS_START_Y = 356;
    private static final int POW_TEXT_X = 931;
    private static final int POW_TEXT_START_Y = 352;
    private static final int POW_X = 927;
    private static final int POW_START_Y = 266;
    private static final int[] POW_SIZE = {142, 142};
    private static final int RIGHT_X = 1502;
    private static final int RIGHT_START_Y = 505;
    private static final int RIGHT_VALUE_Y_OFFSET = 78;
    private static final int RIGHT_Y_SPACING = 198;

    private final QuestData questData;
    private final List<QuestType> quests;

    private boolean allQuestsDone = true;

    public QuestsImageGenerator(
        int page,
        BoarUser boarUser,
        List<BadgeData> badges,
        String firstJoinedDate,
        QuestData questData,
        List<QuestType> quests
    ) {
        super(page, boarUser, badges, firstJoinedDate);
        this.questData = questData;
        this.quests = quests;
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        String underlayPath = PATHS.getMegaMenuAssets() + PATHS.getQuestUnderlay();

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN, IMAGE_SIZE);

        this.textDrawer = new TextDrawer(
            g2d, "", ORIGIN, Align.LEFT, COLORS.get("font"), NUMS.getFontMedium(), QUEST_WIDTH
        );

        for (int i=0; i<this.quests.size(); i++) {
            this.drawQuest(g2d, i);
        }

        int[] resetLabelPos = {RIGHT_X, RIGHT_START_Y};
        String resetStr = TimeUtil.getTimeDistance(TimeUtil.getQuestResetMilli(), false);
        resetStr = Character.toUpperCase(resetStr.charAt(0)) + resetStr.substring(1);
        int[] resetPos = {RIGHT_X, resetLabelPos[1] + RIGHT_VALUE_Y_OFFSET};

        int[] completeLabelPos = {RIGHT_X, resetLabelPos[1] + RIGHT_Y_SPACING};
        String completeStr = "%,d".formatted(this.questData.questsCompleted());
        int[] completePos = {RIGHT_X, completeLabelPos[1] + RIGHT_VALUE_Y_OFFSET};

        int[] fullCompleteLabelPos = {RIGHT_X, completeLabelPos[1] + RIGHT_Y_SPACING};
        String fullCompleteStr = "%,d".formatted(this.questData.perfectWeeks());
        int[] fullCompletePos = {RIGHT_X, fullCompleteLabelPos[1] + RIGHT_VALUE_Y_OFFSET};

        int[] bonusLabelPos = {RIGHT_X, fullCompleteLabelPos[1] + RIGHT_Y_SPACING};

        String bonusStr = STRS.getQuestNoBonusLabel();
        if (this.allQuestsDone && this.questData.fullClaimed()) {
            bonusStr = STRS.getQuestClaimedBonusLabel();
        } else if (this.allQuestsDone) {
            bonusStr = STRS.getQuestUnclaimedBonusLabel();
        }

        int[] bonusPos = {RIGHT_X, bonusLabelPos[1] + RIGHT_VALUE_Y_OFFSET};

        this.textDrawer.setAlign(Align.CENTER);

        TextUtil.drawLabel(this.textDrawer, STRS.getQuestResetLabel(), resetLabelPos);
        TextUtil.drawValue(this.textDrawer, resetStr, resetPos);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsQuestsCompletedLabel(), completeLabelPos);
        TextUtil.drawValue(this.textDrawer, completeStr, completePos);

        TextUtil.drawLabel(this.textDrawer, STRS.getStatsFullQuestsCompletedLabel(), fullCompleteLabelPos);
        TextUtil.drawValue(this.textDrawer, fullCompleteStr, fullCompletePos);

        TextUtil.drawLabel(this.textDrawer, STRS.getQuestBonusLabel(), bonusLabelPos);
        TextUtil.drawValue(this.textDrawer, bonusStr, bonusPos);

        this.drawTopInfo();
        return this;
    }

    private void drawQuest(Graphics2D g2d, int index) throws IOException, URISyntaxException {
        this.drawQuestStr(index);
        this.drawQuestValue(index);
        this.drawReward(g2d, index);
    }

    private void drawQuestStr(int index) {
        String questStr = this.getQuestStr(this.quests.get(index), index);

        this.textDrawer.setText(questStr);
        this.textDrawer.setFontSize(NUMS.getFontSmallMedium());
        this.textDrawer.setPos(new int[] {QUEST_X, QUEST_START_Y + index * QUEST_Y_SPACING});
        this.textDrawer.setAlign(Align.LEFT);
        this.textDrawer.setWidth(QUEST_WIDTH);

        this.textDrawer.drawText();
    }

    private String getQuestStr(QuestType quest, int index) {
        QuestConfig questConfig = CONFIG.getQuestConfig().get(quest.toString());
        String requirement = questConfig.getQuestVals()[index/2].getRequirement();

        return switch (quest) {
            case QuestType.DAILY, QuestType.CLONE_BOARS, QuestType.POW_WIN -> {
                boolean isMultiple = Integer.parseInt(requirement) > 1;

                yield isMultiple
                    ? questConfig.getDescriptionAlt().formatted(requirement)
                    : questConfig.getDescription().formatted(requirement);
            }

            case QuestType.COLLECT_RARITY, QuestType.CLONE_RARITY -> {
                char firstChar = requirement.charAt(0);
                boolean isVowel = firstChar == 'a' || firstChar == 'e' || firstChar == 'i' || firstChar == 'o' ||
                    firstChar == 'u';
                String rarityName = RARITIES.get(requirement).getName();

                yield isVowel
                    ? questConfig.getDescriptionAlt().formatted(requirement, rarityName)
                    : questConfig.getDescription().formatted(requirement, rarityName);
            }

            case QuestType.SPEND_BUCKS, QuestType.COLLECT_BUCKS, QuestType.POW_FAST ->
                questConfig.getDescription().formatted(requirement);

            case QuestType.SEND_GIFTS, QuestType.OPEN_GIFTS -> {
                boolean isMultiple = Integer.parseInt(requirement) > 1;
                String giftStr = isMultiple
                    ? POWS.get("gift").getPluralName()
                    : POWS.get("gift").getName();

                yield questConfig.getDescription().formatted(requirement, giftStr);
            }
        };
    }

    private void drawQuestValue(int index) {
        String questValue = this.getQuestValue(
            this.quests.get(index), this.questData.questProgress().get(index), index
        );

        this.textDrawer.setText(questValue);
        this.textDrawer.setFontSize(NUMS.getFontMedium());
        this.textDrawer.setPos(new int[] {QUEST_X, QUEST_START_Y + index * QUEST_Y_SPACING + QUEST_VALUE_Y_OFFSET});
        this.textDrawer.setWidth(-1);

        this.textDrawer.drawText();
    }

    private String getQuestValue(QuestType quest, int progress, int index) {
        int requirementAmt = QuestUtil.getRequiredAmt(quest, index);
        String questValue = "<>%s<>%d/%d";

        boolean requirementMet = quest.equals(QuestType.POW_FAST) == (progress < requirementAmt);
        String colorKey = requirementMet ? "green" : "silver";

        if (!requirementMet) {
            this.allQuestsDone = false;
        }

        return questValue.formatted(colorKey, progress, requirementAmt);
    }

    private void drawReward(Graphics2D g2d, int index) throws IOException, URISyntaxException {
        if (this.questData.questClaims().get(index)) {
            String checkPath = PATHS.getOtherAssets() + PATHS.getCheckMark();
            GraphicsUtil.drawImage(g2d, checkPath, new int[] {POW_X, POW_START_Y + index * QUEST_Y_SPACING}, POW_SIZE);
            return;
        }

        IndivQuestConfig indivQuestConfig = CONFIG.getQuestConfig().get(this.quests.get(index).toString())
            .getQuestVals()[index/2];
        String rewardType = indivQuestConfig.getRewardType();
        int rewardAmt = indivQuestConfig.getRewardAmt();

        if (rewardType.equals("bucks")) {
            this.textDrawer.setText("+<>bucks<>$%d".formatted(rewardAmt));
            this.textDrawer.setFontSize(NUMS.getFontMedium());
            this.textDrawer.setPos(new int[] {BUCKS_X, BUCKS_START_Y + index * QUEST_Y_SPACING});
            this.textDrawer.setAlign(Align.RIGHT);
            this.textDrawer.setWidth(-1);

            this.textDrawer.drawText();
            return;
        }

        String filePath = PATHS.getPowerups() + POWS.get(rewardType).getFile();

        this.textDrawer.setText("+<>powerup<>%d".formatted(rewardAmt));
        this.textDrawer.setFontSize(NUMS.getFontSmallest());
        this.textDrawer.setPos(new int[] {POW_TEXT_X, POW_TEXT_START_Y + index * QUEST_Y_SPACING});
        this.textDrawer.setAlign(Align.RIGHT);
        this.textDrawer.setWidth(-1);

        this.textDrawer.drawText();

        GraphicsUtil.drawImage(g2d, filePath, new int[] {POW_X, POW_START_Y + index * QUEST_Y_SPACING}, POW_SIZE);
    }
}
