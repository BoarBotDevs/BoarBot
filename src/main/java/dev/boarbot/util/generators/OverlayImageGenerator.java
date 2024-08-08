package dev.boarbot.util.generators;

import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.TextDrawer;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class OverlayImageGenerator extends ImageGenerator {
    private String text;

    public OverlayImageGenerator(BufferedImage image, String text) {
        this.generatedImage = image;
        this.text = text;
    }

    public OverlayImageGenerator generate() {
        Graphics2D g2d = this.generatedImage.createGraphics();

        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fill(new RoundRectangle2D.Double(
            0,
            0,
            this.generatedImage.getWidth(),
            this.generatedImage.getHeight(),
            this.config.getNumberConfig().getBorder() * 2,
            this.config.getNumberConfig().getBorder() * 2
        ));

        float fontSize = this.config.getNumberConfig().getFontBig();
        int[] center = {this.generatedImage.getWidth() / 2, this.generatedImage.getHeight() / 2 + (int) (fontSize / 2)};
        String fontColor = this.config.getColorConfig().get("font");

        TextDrawer textDrawer = new TextDrawer(
            g2d,
            this.text,
            center,
            Align.CENTER,
            fontColor,
            fontSize,
            (int) (this.generatedImage.getWidth() * 0.9),
            true
        );
        textDrawer.drawText();

        return this;
    }
}
