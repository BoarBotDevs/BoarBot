package dev.boarbot.util.generators;

import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.TextDrawer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class GiftImageGenerator extends ImageGenerator {
    private static final int FROM_Y = 943;

    private final String username;

    public GiftImageGenerator(String username) {
        this.username = username;
    }

    @Override
    public ImageGenerator generate() throws IOException {
        this.generatedImage = ImageIO.read(
            new File(this.config.getPathConfig().getOtherAssets() + this.config.getPathConfig().getGiftImage())
        );
        Graphics2D g2d = this.generatedImage.createGraphics();

        TextDrawer textDrawer = new TextDrawer(
            g2d,
            "From: " + this.username,
            new int[] {this.generatedImage.getWidth() / 2, FROM_Y},
            Align.CENTER,
            this.config.getColorConfig().get("silver"),
            this.config.getNumberConfig().getFontBig(),
            this.generatedImage.getWidth() - this.config.getNumberConfig().getBorder() * 4
        );
        textDrawer.drawText();

        return this;
    }
}
