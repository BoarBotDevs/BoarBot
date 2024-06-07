package dev.boarbot.util.generators;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.TextDrawer;
import lombok.Setter;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EmbedGenerator {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    private String str;
    private String pureStr;
    @Setter private String color;

    private final Font font = BoarBotApp.getBot().getFont()
        .deriveFont((float) this.config.getNumberConfig().getFontBig());

    public EmbedGenerator(String str) {
        this(str, null);
    }

    public EmbedGenerator(String str, String color) {
        this.str = str;
        this.pureStr = str.replaceAll("//(.*?)//", "");
        this.color = color == null ? this.config.getColorConfig().get("font") : color;
    }

    public void setStr(String str) {
        this.str = str;
        this.pureStr = str.replaceAll("//(.*?)//", "");
    }

    public FileUpload generate() throws IOException {
        NumberConfig nums = this.config.getNumberConfig();

        BufferedImage generatedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        g2d.setFont(this.font);
        FontMetrics fm = g2d.getFontMetrics();

        int width = Math.min(fm.stringWidth(this.pureStr) + nums.getBorder() * 6, nums.getEmbedMaxWidth());

        TextDrawer textDrawer = new TextDrawer(
            g2d,
            this.str,
            new int[]{0,0},
            Align.CENTER,
            this.color,
            nums.getFontBig(),
            width - nums.getBorder() * 6,
            true
        );

        int height = (int) textDrawer.drawText() + nums.getBorder() * 8;

        generatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = generatedImage.createGraphics();

        g2d.setFont(this.font);
        g2d.setColor(Color.decode(this.config.getColorConfig().get("dark")));

        g2d.fillRoundRect(0, 0, width, height, nums.getBorder(), nums.getBorder());

        int[] pos = new int[] {
            width / 2,
            height / 2 + (int) ((fm.getAscent()) * 0.5)
        };

        textDrawer.setG2d(g2d);
        textDrawer.setPos(pos);
        textDrawer.drawText();

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        ImageIO.write(generatedImage, "png", byteArrayOS);

        return FileUpload.fromData(byteArrayOS.toByteArray(), "unknown.png");
    }
}
