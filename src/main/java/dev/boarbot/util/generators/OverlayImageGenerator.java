package dev.boarbot.util.generators;

import com.google.gson.Gson;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.python.PythonUtil;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

public class OverlayImageGenerator extends ImageGenerator {
    private String text;

    private BufferedImage overlayImage;
    private int[] pos;

    private byte[] animatedImage;
    private int[] size;

    private final boolean darken;

    public OverlayImageGenerator(BufferedImage image, String text) {
        this.generatedImage = image;
        this.text = text;
        this.darken = true;
    }

    public OverlayImageGenerator(BufferedImage image, BufferedImage overlayImage) {
        this(image, overlayImage, true);
    }

    public OverlayImageGenerator(BufferedImage image, BufferedImage overlayImage, boolean darken) {
        this(image, overlayImage, null, darken);
    }

    public OverlayImageGenerator(BufferedImage image, BufferedImage overlayImage, int[] pos) {
        this(image, overlayImage, pos, true);
    }

    public OverlayImageGenerator(BufferedImage image, BufferedImage overlayImage, int[] pos, boolean darken) {
        this.generatedImage = image;
        this.overlayImage = overlayImage;
        this.pos = pos;
        this.darken = darken;
    }

    public OverlayImageGenerator(BufferedImage image, String path, int[] size) throws URISyntaxException, IOException {
        this(image, path, size, true);
    }

    public OverlayImageGenerator(
        BufferedImage image, String path, int[] size, boolean darken
    ) throws URISyntaxException, IOException {
        this(image, path, size, null, darken);
    }

    public OverlayImageGenerator(
        BufferedImage image, String path, int[] size, int[] pos
    ) throws URISyntaxException, IOException {
        this(image, path, size, pos, true);
    }

    public OverlayImageGenerator(
        BufferedImage image, String path, int[] size, int[] pos, boolean darken
    ) throws URISyntaxException, IOException {
        this.generatedImage = image;
        this.animatedImage = GraphicsUtil.getImageBytes(path);
        this.animated = true;
        this.size = size;
        this.pos = pos;
        this.darken = darken;
    }

    @Override
    public OverlayImageGenerator generate() throws IOException {
        Graphics2D g2d = this.generatedImage.createGraphics();

        if (this.darken) {
            g2d.setColor(new Color(0, 0, 0, 0.8f));
            g2d.fill(new RoundRectangle2D.Double(
                0,
                0,
                this.generatedImage.getWidth(),
                this.generatedImage.getHeight(),
                NUMS.getBorder() * 2,
                NUMS.getBorder() * 2
            ));
        }

        if (this.text != null) {
            return drawText(g2d);
        } else if (this.animated) {
            return drawImage(g2d);
        }

        return this;
    }

    public OverlayImageGenerator setBaseImage(BufferedImage baseImage) {
        this.generatedImage = baseImage;
        return this;
    }

    private OverlayImageGenerator drawText(Graphics2D g2d) {
        float fontSize = NUMS.getFontBig();
        int[] pos = this.pos == null
            ? new int[] {this.generatedImage.getWidth() / 2, this.generatedImage.getHeight() / 2 + (int) (fontSize / 2)}
            : this.pos;
        String fontColor = COLORS.get("font");

        TextDrawer textDrawer = new TextDrawer(
            g2d,
            this.text,
            pos,
            Align.CENTER,
            fontColor,
            fontSize,
            (int) (this.generatedImage.getWidth() * 0.9),
            true
        );
        textDrawer.drawText();

        return this;
    }

    private OverlayImageGenerator drawImage(Graphics2D g2d) throws IOException {
        int[] pos = this.pos;

        if (pos == null) {
            pos = new int[] {this.generatedImage.getWidth() / 2, this.generatedImage.getHeight() / 2};

            if (this.overlayImage != null) {
                pos[0] = pos[0] - this.overlayImage.getWidth() / 2;
                pos[1] = pos[1] - this.overlayImage.getHeight() / 2;
            } else {
                pos[0] = pos[0] - this.size[0] / 2;
                pos[1] = pos[1] - this.size[1] / 2;
            }
        }

        if (this.animated) {
            Gson g = new Gson();

            Process pythonProcess = new ProcessBuilder(
                "python",
                PATHS.getApplyScript(),
                g.toJson(NUMS),
                "[%d, %d]".formatted(pos[0], pos[1]),
                "[%d, %d]".formatted(this.size[0], this.size[1]),
                Integer.toString(this.getBytes().length),
                Integer.toString(this.animatedImage.length)
            ).start();

            this.generatedImageBytes = PythonUtil.getResult(pythonProcess, this.getBytes() ,this.animatedImage);
        } else {
            g2d.drawImage(this.overlayImage, pos[0], pos[1], null);
        }

        return this;
    }
}
