package dev.boarbot.util.graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {
    public static File toFile(BufferedImage image) throws IOException {
        File temp = File.createTempFile("image", ".png");
        ImageIO.write(image, "PNG", temp);
        return temp;
    }
}
