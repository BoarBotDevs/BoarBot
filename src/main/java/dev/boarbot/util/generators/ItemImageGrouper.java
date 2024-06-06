package dev.boarbot.util.generators;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.util.graphics.GraphicsUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

@Log4j2
public final class ItemImageGrouper {
    public static FileUpload groupItems(
        List<ItemImageGenerator> itemGens, int page
    ) throws IOException, URISyntaxException {
        BotConfig config = BoarBotApp.getBot().getConfig();
        NumberConfig nums = config.getNumberConfig();
        PathConfig pathConfig = config.getPathConfig();

        byte[] leftImageBytes = null;
        byte[] middleImageBytes = itemGens.get(page).generate();
        byte[] rightImageBytes = null;
        byte[] resultByteArray;

        String extension = itemGens.get(page).getFilePath().split("[.]")[1];

        if (page != 0) {
            leftImageBytes = itemGens.get(page-1).generate();
        }

        if (page != itemGens.size()-1) {
            rightImageBytes = itemGens.get(page+1).generate();
        }

        if (leftImageBytes == null && rightImageBytes == null) {
            return FileUpload.fromData(middleImageBytes, "unknown." + extension);
        }

        int[] imageSize = nums.getItemImageSize();
        int[] smallImageSize = {(int) (imageSize[0] * .9), (int) (imageSize[1] * .9)};

        int horizPadding = nums.getItemHorizPadding();
        int smallYPos = (imageSize[1] - smallImageSize[1]) / 2;

        BufferedImage groupedImage = new BufferedImage(
            imageSize[0] + horizPadding*2, imageSize[1], BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = groupedImage.createGraphics();

        ByteArrayInputStream byteArrayIS;

        if (leftImageBytes != null) {
            byteArrayIS = new ByteArrayInputStream(leftImageBytes);
            BufferedImage firstImage = ImageIO.read(byteArrayIS);

            g2d.drawImage(firstImage, 0, smallYPos, smallImageSize[0], smallImageSize[1], null);
            GraphicsUtil.drawImage(
                g2d,
                pathConfig.getItemAssets() + pathConfig.getItemShadowLeft(),
                new int[]{horizPadding-nums.getItemShadowWidth(), smallYPos},
                new int[]{nums.getItemShadowWidth(), smallImageSize[1]}
            );
        }

        if (rightImageBytes != null) {
            byteArrayIS = new ByteArrayInputStream(rightImageBytes);
            BufferedImage lastImage = ImageIO.read(byteArrayIS);

            g2d.drawImage(
                lastImage,
                imageSize[0] + horizPadding*2 - smallImageSize[0],
                smallYPos,
                smallImageSize[0],
                smallImageSize[1],
                null
            );
            GraphicsUtil.drawImage(
                g2d,
                pathConfig.getItemAssets() + pathConfig.getItemShadowRight(),
                new int[]{horizPadding + imageSize[0], smallYPos},
                new int[]{nums.getItemShadowWidth(), smallImageSize[1]}
            );
        }

        if (extension.equals("gif")) {
            Gson g = new Gson();

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(groupedImage, "png", byteArrayOS);
            resultByteArray = byteArrayOS.toByteArray();

            Process pythonProcess = new ProcessBuilder(
                "python",
                "src/main/resources/scripts/animated_item_grouper.py",
                g.toJson(config.getNumberConfig()),
                Integer.toString(resultByteArray.length),
                Integer.toString(middleImageBytes.length)
            ).start();

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));
            OutputStream stdOut = pythonProcess.getOutputStream();

            stdOut.write(resultByteArray);
            stdOut.write(middleImageBytes);
            stdOut.close();

            String result = stdIn.readLine();

            if (result == null) {
                String tempErrMessage;
                String errMessage = "";

                while ((tempErrMessage = stdErr.readLine()) != null) {
                    errMessage = errMessage.concat(tempErrMessage + "\n");
                }

                log.error(errMessage);
            }

            resultByteArray = Base64.getDecoder().decode(result);
        } else {
            byteArrayIS = new ByteArrayInputStream(middleImageBytes);
            BufferedImage mainImage = ImageIO.read(byteArrayIS);

            g2d.drawImage(mainImage, horizPadding, 0, imageSize[0], imageSize[1], null);

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ImageIO.write(groupedImage, "png", byteArrayOS);
            resultByteArray = byteArrayOS.toByteArray();
        }

        return FileUpload.fromData(resultByteArray, "unknown." + extension);
    }
}
