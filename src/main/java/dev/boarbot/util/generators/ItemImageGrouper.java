package dev.boarbot.util.generators;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.python.PythonUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
public final class ItemImageGrouper implements Configured {
    private static final int[] IMAGE_SIZE = ItemImageGenerator.IMAGE_SIZE;
    private static final int HORIZ_PADDING = 135;
    private static final int SHADOW_WIDTH = 35;

    public static FileUpload groupItems(
        List<ItemImageGenerator> itemGens, int page
    ) throws IOException, URISyntaxException {
        byte[] leftImageBytes = null;
        byte[] middleImageBytes = itemGens.get(page).generate().getBytes();
        byte[] rightImageBytes = null;
        byte[] resultByteArray;

        String extension = itemGens.get(page).getFilePath().split("[.]")[1];

        if (page != 0) {
            leftImageBytes = itemGens.get(page-1).generate(true).getBytes();
        }

        if (page != itemGens.size()-1) {
            rightImageBytes = itemGens.get(page+1).generate(true).getBytes();
        }

        if (leftImageBytes == null && rightImageBytes == null) {
            return FileUpload.fromData(middleImageBytes, "unknown." + extension);
        }

        int[] smallImageSize = {(int) (IMAGE_SIZE[0] * .9), (int) (IMAGE_SIZE[1] * .9)};
        int smallYPos = (IMAGE_SIZE[1] - smallImageSize[1]) / 2;

        BufferedImage groupedImage = new BufferedImage(
            IMAGE_SIZE[0] + HORIZ_PADDING*2, IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = groupedImage.createGraphics();

        ByteArrayInputStream byteArrayIS;

        if (leftImageBytes != null) {
            byteArrayIS = new ByteArrayInputStream(leftImageBytes);
            BufferedImage firstImage = ImageIO.read(byteArrayIS);

            g2d.drawImage(firstImage, 0, smallYPos, smallImageSize[0], smallImageSize[1], null);

            int[] shadowPos = {HORIZ_PADDING-SHADOW_WIDTH, smallYPos};
            int[] shadowSize = {SHADOW_WIDTH, smallImageSize[1]};

            GraphicsUtil.drawImage(
                g2d, PATHS.getItemAssets() + PATHS.getItemShadowLeft(), shadowPos, shadowSize
            );
        }

        if (rightImageBytes != null) {
            byteArrayIS = new ByteArrayInputStream(rightImageBytes);
            BufferedImage lastImage = ImageIO.read(byteArrayIS);

            g2d.drawImage(
                lastImage,
                IMAGE_SIZE[0] + HORIZ_PADDING*2 - smallImageSize[0],
                smallYPos,
                smallImageSize[0],
                smallImageSize[1],
                null
            );

            int[] shadowPos = {HORIZ_PADDING + IMAGE_SIZE[0], smallYPos};
            int[] shadowSize = {SHADOW_WIDTH, smallImageSize[1]};

            GraphicsUtil.drawImage(
                g2d, PATHS.getItemAssets() + PATHS.getItemShadowRight(), shadowPos, shadowSize
            );
        }

        if (extension.equals("gif")) {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(groupedImage, "png", byteArrayOS);
            resultByteArray = byteArrayOS.toByteArray();

            Process pythonProcess = new ProcessBuilder(
                "python",
                PATHS.getGroupScript(),
                Integer.toString(resultByteArray.length),
                Integer.toString(middleImageBytes.length)
            ).start();

            resultByteArray = PythonUtil.getResult(pythonProcess, resultByteArray, middleImageBytes);
        } else {
            byteArrayIS = new ByteArrayInputStream(middleImageBytes);
            BufferedImage mainImage = ImageIO.read(byteArrayIS);

            g2d.drawImage(mainImage, HORIZ_PADDING, 0, IMAGE_SIZE[0], IMAGE_SIZE[1], null);

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(groupedImage, "png", byteArrayOS);
            resultByteArray = byteArrayOS.toByteArray();
        }

        return FileUpload.fromData(resultByteArray, "unknown." + extension);
    }
}
