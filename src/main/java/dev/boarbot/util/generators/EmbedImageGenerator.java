package dev.boarbot.util.generators;

import dev.boarbot.BoarBotApp;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.TextDrawer;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class EmbedImageGenerator extends ImageGenerator {
    private final static int MAX_WIDTH = 1500;

    private String str;
    private String pureStr;
    @Setter private String color;

    private final Font font = BoarBotApp.getBot().getFont().deriveFont((float) NUMS.getFontBig());

    public EmbedImageGenerator(String str) {
        this(str, null);
    }

    public EmbedImageGenerator(String str, String color) {
        this.str = str;
        this.pureStr = str.replaceAll("<>(.*?)<>", "");
        this.color = color == null ? COLORS.get("font") : color;
    }

    public void setStr(String str) {
        this.str = str;
        this.pureStr = str.replaceAll("<>(.*?)<>", "");
    }

    @Override
    public EmbedImageGenerator generate() throws IOException {
        this.generatedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        g2d.setFont(this.font);
        FontMetrics fm = g2d.getFontMetrics();

        int width = Math.min(fm.stringWidth(this.pureStr) + NUMS.getBorder() * 6, MAX_WIDTH);

        TextDrawer textDrawer = new TextDrawer(
            g2d,
            this.str,
            ORIGIN,
            Align.CENTER,
            this.color,
            NUMS.getFontBig(),
            width - NUMS.getBorder() * 6,
            true
        );

        int height = (int) textDrawer.drawText() + NUMS.getBorder() * 8;

        this.generatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = this.generatedImage.createGraphics();

        g2d.setFont(this.font);
        g2d.setColor(Color.decode(COLORS.get("dark")));

        g2d.fillRoundRect(0, 0, width, height, NUMS.getBorder(), NUMS.getBorder());

        int[] pos = new int[] {
            width / 2,
            height / 2 + (int) ((fm.getAscent()) * 0.5)
        };

        textDrawer.setG2d(g2d);
        textDrawer.setPos(pos);
        textDrawer.drawText();

        return this;
    }
}
