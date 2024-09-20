package dev.boarbot.util.generators;

import dev.boarbot.bot.config.prompts.PromptConfig;
import dev.boarbot.events.PromptType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

public class PowerupEventImageGenerator extends ImageGenerator {
    private final static int[] IMAGE_SIZE = {1491, 1069};
    private final static int[] ICON_SIZE = {150, 150};
    private final static int TEXT_X = 746;
    private final static int REWARD_Y = 1021;
    private final static int EVENT_LABEL_Y = 156;
    private final static int EVENT_DESC_Y = 561;
    private final static int END_START_Y = 352;
    private final static int END_VALUE_Y_OFFSET = 71;
    private final static int END_LABEL_Y_SPACING = 188;

    private String fastestStr;
    private String avgStr;

    private final String promptID;
    private final String promptStr;
    private final String powerupID;

    public PowerupEventImageGenerator(PromptType promptType, String promptID, String powerupID) {
        PromptConfig promptConfig = CONFIG.getPromptConfig().get(promptType.toString());

        this.promptID = promptID;
        this.promptStr = switch (promptType) {
            case PromptType.EMOJI_FIND, PromptType.FAST -> promptConfig.getDescription();
            case PromptType.TRIVIA -> promptConfig.getPrompts().get(promptID).getDescription();
            case PromptType.CLOCK -> promptConfig.getDescription().formatted(promptConfig.getPrompts().get(promptID)
                .getName());
        };
        this.powerupID = powerupID;
    }

    @Override
    public ImageGenerator generate() throws IOException, URISyntaxException {
        boolean isEnd = this.generatedImageBytes != null;
        this.generatedImageBytes = null;

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.eventUnderlayPath, ORIGIN);

        GraphicsUtil.drawImage(
            g2d, ResourceUtil.powIconPath, new int[] {NUMS.getBorder() * 2, NUMS.getBorder() * 2}, ICON_SIZE
        );
        GraphicsUtil.drawImage(
            g2d,
            ResourceUtil.powIconPath,
            new int[] {this.generatedImage.getWidth() - NUMS.getBorder() * 2, NUMS.getBorder() * 2},
            new int[] {ICON_SIZE[0] * -1, ICON_SIZE[1]}
        );

        int eventAmt = POWS.get(this.powerupID).getEventAmt();
        String powStr = eventAmt == 1
            ? POWS.get(this.powerupID).getName()
            : POWS.get(this.powerupID).getPluralName() ;

        this.textDrawer = new TextDrawer(
            g2d,
            STRS.getPowEventReward().formatted(eventAmt, powStr),
            new int[] {TEXT_X, REWARD_Y},
            Align.CENTER,
            COLORS.get("font"),
            NUMS.getFontBig()
        );

        this.textDrawer.drawText();

        this.textDrawer.setPos(new int[] {TEXT_X, EVENT_LABEL_Y});
        this.textDrawer.setFontSize(NUMS.getFontHuge());

        if (isEnd) {
            return this.generateEnd();
        }

        return this.generateSpawn();
    }

    private ImageGenerator generateSpawn() {
        this.textDrawer.setText(STRS.getPowEventLabel());
        this.textDrawer.drawText();

        this.drawMiddleText(this.promptStr);

        return this;
    }

    private ImageGenerator generateEnd() {
        this.textDrawer.setText(STRS.getPowEventEndLabel());
        this.textDrawer.drawText();

        if (this.fastestStr != null && this.avgStr != null) {
            this.textDrawer.setFontSize(NUMS.getFontBig());
            this.textDrawer.setWidth(this.generatedImage.getWidth() - NUMS.getBorder() * 4);
            this.textDrawer.setWrap(true);

            int[] fastestPos = {TEXT_X, END_START_Y};
            int[] avgPos = {TEXT_X, fastestPos[1] + END_LABEL_Y_SPACING};
            int[] promptPos = {TEXT_X, avgPos[1] + END_LABEL_Y_SPACING};

            this.drawGroup(STRS.getPowEventFastLabel(), this.fastestStr, fastestPos);
            this.drawGroup(STRS.getPowEventAvgLabel(), this.avgStr, avgPos);
            this.drawGroup(STRS.getPowEventPromptLabel(), BoarUtil.getPromptStr(this.promptID), promptPos);
        } else {
            this.drawMiddleText(STRS.getPowEventNobody());
        }

        return this;
    }

    private void drawMiddleText(String str) {
        this.textDrawer.setText(str);
        this.textDrawer.setPos(new int[] {TEXT_X, EVENT_DESC_Y});
        this.textDrawer.setFontSize(NUMS.getFontMedium());
        this.textDrawer.setWidth(this.generatedImage.getWidth() - NUMS.getBorder() * 16);
        this.textDrawer.setWrap(true);
        this.textDrawer.drawText();
    }

    private void drawGroup(String label, String value, int[] pos) {
        this.textDrawer.setText(label);
        this.textDrawer.setPos(pos);
        this.textDrawer.setColorVal(COLORS.get("font"));
        this.textDrawer.drawText();

        this.textDrawer.setText(value);
        this.textDrawer.setPos(new int[] {pos[0], pos[1] + END_VALUE_Y_OFFSET});
        this.textDrawer.setColorVal(COLORS.get("silver"));
        this.textDrawer.drawText();
    }

    public ImageGenerator setEndStrings(String fastestStr, String avgStr) {
        this.fastestStr = fastestStr;
        this.avgStr = avgStr;
        return this;
    }
}
