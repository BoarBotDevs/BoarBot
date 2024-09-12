package dev.boarbot.util.generators;

import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

public class GiftImageGenerator extends ImageGenerator {
    private static final int[] IMAGE_SIZE = {930, 1080};
    private static final int FROM_Y = 943;

    private final String username;

    public GiftImageGenerator(String username) {
        this.username = username;
    }

    @Override
    public ImageGenerator generate() throws IOException, URISyntaxException {
        String underlayPath = PATHS.getOtherAssets() + PATHS.getGiftImage();

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN);

        TextDrawer textDrawer = new TextDrawer(
            g2d,
            "From: " + this.username,
            new int[] {this.generatedImage.getWidth() / 2, FROM_Y},
            Align.CENTER,
            COLORS.get("silver"),
            NUMS.getFontBig(),
            this.generatedImage.getWidth() - NUMS.getBorder() * 4
        );
        textDrawer.drawText();

        return this;
    }
}
