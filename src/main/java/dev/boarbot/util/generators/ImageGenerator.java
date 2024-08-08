package dev.boarbot.util.generators;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public abstract class ImageGenerator {
    protected BotConfig config = BoarBotApp.getBot().getConfig();

    protected byte[] generatedImageBytes;
    protected BufferedImage generatedImage;

    public abstract ImageGenerator generate() throws IOException, URISyntaxException;

    public BufferedImage getImage() throws IOException {
        if (this.generatedImage == null && this.generatedImageBytes != null) {
            ByteArrayInputStream byteArrayIS;
            byteArrayIS = new ByteArrayInputStream(this.generatedImageBytes);
            this.generatedImage = ImageIO.read(byteArrayIS);
        }

        return this.generatedImage;
    }

    public byte[] getBytes() throws IOException {
        if (this.generatedImageBytes == null && this.generatedImage != null) {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(this.generatedImage, "png", byteArrayOS);
            this.generatedImageBytes = byteArrayOS.toByteArray();
        }

        return this.generatedImageBytes;
    }

    public FileUpload getFileUpload() throws IOException {
        return FileUpload.fromData(this.getBytes(), "unknown.png");
    }
}
