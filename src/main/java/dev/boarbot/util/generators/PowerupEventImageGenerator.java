package dev.boarbot.util.generators;

import dev.boarbot.bot.config.prompts.PromptConfig;
import dev.boarbot.events.PromptType;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

public class PowerupEventImageGenerator extends ImageGenerator {
    private final static int[] IMAGE_SIZE = {1491, 1069};

    private final String promptStr;
    private final String powerupID;

    public PowerupEventImageGenerator(PromptType promptType, String promptID, String powerupID) {
        PromptConfig promptConfig = CONFIG.getPromptConfig().get(promptType.toString());

        this.promptStr = switch (promptType) {
            case PromptType.EMOJI_FIND, PromptType.FAST -> promptConfig.getDescription();
            case PromptType.TRIVIA -> promptConfig.getPrompts().get(powerupID).getDescription();
            case PromptType.CLOCK -> promptConfig.getDescription().formatted(promptConfig.getPrompts().get(promptID).getName());
        };
        this.powerupID = powerupID;
    }

    @Override
    public ImageGenerator generate() throws IOException, URISyntaxException {
        String underlayPath = PATHS.getOtherAssets() + PATHS.getEventUnderlay();

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN);

        if (this.textDrawer == null) {
            this.textDrawer = new TextDrawer(g2d, "", ORIGIN, Align.LEFT, COLORS.get("font"), NUMS.getFontMedium());
        }

        textDrawer.drawText();

        return this;
    }
}
