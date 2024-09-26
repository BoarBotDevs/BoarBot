package dev.boarbot.util.generators;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.graphics.TextDrawer;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public abstract class ImageGenerator implements Configured {
    protected static final int[] ORIGIN = {0, 0};

    protected byte[] generatedImageBytes;
    protected BufferedImage generatedImage;
    protected boolean animated = false;

    protected TextDrawer textDrawer;

    public abstract ImageGenerator generate() throws IOException, URISyntaxException;

    public BufferedImage getImage() throws IOException {
        if (this.generatedImage == null && this.generatedImageBytes == null) {
            throw new IllegalStateException("No image has been generated");
        }

        if (this.generatedImage == null) {
            ByteArrayInputStream byteArrayIS;
            byteArrayIS = new ByteArrayInputStream(this.generatedImageBytes);
            this.generatedImage = ImageIO.read(byteArrayIS);
        }

        return this.generatedImage;
    }

    public byte[] getBytes() throws IOException {
        if (this.generatedImage == null && this.generatedImageBytes == null) {
            throw new IllegalStateException("No image has been generated");
        }

        if (this.generatedImageBytes == null) {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(this.generatedImage, "png", byteArrayOS);
            this.generatedImageBytes = byteArrayOS.toByteArray();
        }

        return this.generatedImageBytes;
    }

    public FileUpload getFileUpload() throws IOException {
        if (this.generatedImage == null && this.generatedImageBytes == null) {
            throw new IllegalStateException("No image has been generated");
        }

        return FileUpload.fromData(this.getBytes(), "unknown" + (this.animated ? ".gif" : ".png"));
    }
}
