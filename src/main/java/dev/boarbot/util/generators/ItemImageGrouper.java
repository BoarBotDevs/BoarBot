package dev.boarbot.util.generators;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.python.PythonUtil;

import dev.boarbot.util.resource.ResourceUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

public final class ItemImageGrouper implements Configured {
    private static final int[] IMAGE_SIZE = ItemImageGenerator.IMAGE_SIZE;
    private static final int HORIZ_PADDING = 135;
    private static final int SHADOW_WIDTH = 35;

    public static String getFileName(List<ItemImageGenerator> itemGens, int page) {
        return "unknown." + itemGens.get(page).getFilePath().split("[.]")[1];
    }

    public static byte[] groupItems(
        List<ItemImageGenerator> itemGens, int page
    ) throws IOException, URISyntaxException {
        BufferedImage leftImage = null;
        ItemImageGenerator middleGen = itemGens.get(page).generate();
        BufferedImage rightImage = null;
        byte[] resultByteArray;

        String extension = itemGens.get(page).getFilePath().split("[.]")[1];

        if (page != 0) {
            leftImage = itemGens.get(page-1).generate(true).getImage();
        }

        if (page != itemGens.size()-1) {
            rightImage = itemGens.get(page+1).generate(true).getImage();
        }

        if (leftImage == null && rightImage == null) {
            return middleGen.getBytes();
        }

        int[] smallImageSize = {(int) (IMAGE_SIZE[0] * .9), (int) (IMAGE_SIZE[1] * .9)};
        int smallYPos = (IMAGE_SIZE[1] - smallImageSize[1]) / 2;

        BufferedImage groupedImage = new BufferedImage(
            IMAGE_SIZE[0] + HORIZ_PADDING*2, IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = groupedImage.createGraphics();

        if (leftImage != null) {
            g2d.drawImage(leftImage, 0, smallYPos, smallImageSize[0], smallImageSize[1], null);

            int[] shadowPos = {HORIZ_PADDING-SHADOW_WIDTH, smallYPos};
            int[] shadowSize = {SHADOW_WIDTH, smallImageSize[1]};

            GraphicsUtil.drawImage(
                g2d, ResourceUtil.itemShadowPathL, shadowPos, shadowSize
            );
        }

        if (rightImage != null) {
            g2d.drawImage(
                rightImage,
                IMAGE_SIZE[0] + HORIZ_PADDING*2 - smallImageSize[0],
                smallYPos,
                smallImageSize[0],
                smallImageSize[1],
                null
            );

            int[] shadowPos = {HORIZ_PADDING + IMAGE_SIZE[0], smallYPos};
            int[] shadowSize = {SHADOW_WIDTH, smallImageSize[1]};

            GraphicsUtil.drawImage(
                g2d, ResourceUtil.itemShadowPathR, shadowPos, shadowSize
            );
        }

        if (extension.equals("webp")) {
            byte[] middleImageBytes = middleGen.getBytes();

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(groupedImage, "png", byteArrayOS);
            resultByteArray = byteArrayOS.toByteArray();

            Process pythonProcess = new ProcessBuilder(
                "python3",
                PythonUtil.getTempPath(ResourceUtil.itemGroupScript),
                Integer.toString(resultByteArray.length),
                Integer.toString(middleImageBytes.length)
            ).start();

            resultByteArray = PythonUtil.getResult(pythonProcess, resultByteArray, middleImageBytes);
        } else {
            g2d.drawImage(middleGen.getImage(), HORIZ_PADDING, 0, IMAGE_SIZE[0], IMAGE_SIZE[1], null);

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(groupedImage, "png", byteArrayOS);
            resultByteArray = byteArrayOS.toByteArray();
        }

        return resultByteArray;
    }
}
