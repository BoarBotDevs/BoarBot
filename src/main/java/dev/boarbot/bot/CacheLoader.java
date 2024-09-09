package dev.boarbot.bot;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.util.graphics.GraphicsUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

@Slf4j
class CacheLoader {
    private final static BotConfig config = BoarBotApp.getBot().getConfig();
    private final static NumberConfig nums = config.getNumberConfig();
    private final static PathConfig pathConfig = config.getPathConfig();

    private final static Map<String, BufferedImage> imageCacheMap = BoarBotApp.getBot().getImageCacheMap();

    private final static int[] ORIGIN = {0, 0};
    private final static int[] LARGE_SIZE = nums.getLargeBoarSize();
    private final static int[] BIG_SIZE = nums.getBigBoarSize();
    private final static int[] MEDIUM_BIG_SIZE = nums.getMediumBigBoarSize();
    private final static int[] MEDIUM_SIZE = nums.getMediumBoarSize();

    public static void loadCache() {
        loadBoars();
        loadBorders();
    }

    private static void loadBoars() {
        log.info("Attempting to load boar images into cache");

        for (String boarID : config.getItemConfig().getBoars().keySet()) {
            try {
                BoarItemConfig boarInfo = config.getItemConfig().getBoars().get(boarID);

                if (boarInfo.getFile().isEmpty()) {
                    throw new IllegalArgumentException("Failed to find file.");
                }

                String filePath = boarInfo.getStaticFile() != null
                    ? pathConfig.getBoars() + boarInfo.getStaticFile()
                    : pathConfig.getBoars() + boarInfo.getFile();

                if (filePath.endsWith(".gif")) {
                    throw new IllegalArgumentException("Animated file is missing a static version.");
                }

                BufferedImage largeBoarImage = new BufferedImage(
                    LARGE_SIZE[0], LARGE_SIZE[1], BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D largeBoarGraphics = largeBoarImage.createGraphics();

                BufferedImage bigBoarImage = new BufferedImage(
                    BIG_SIZE[0], BIG_SIZE[1], BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D bigBoarGraphics = bigBoarImage.createGraphics();

                BufferedImage mediumBigBoarImage = new BufferedImage(
                    MEDIUM_BIG_SIZE[0], MEDIUM_BIG_SIZE[1], BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D mediumBigBoarGraphics = mediumBigBoarImage.createGraphics();

                BufferedImage mediumBoarImage = new BufferedImage(
                    MEDIUM_SIZE[0], MEDIUM_SIZE[1], BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D mediumBoarGraphics = mediumBoarImage.createGraphics();

                GraphicsUtil.drawImage(largeBoarGraphics, filePath, ORIGIN, LARGE_SIZE);
                imageCacheMap.put("large" + boarID, largeBoarImage);

                GraphicsUtil.drawImage(bigBoarGraphics, filePath, ORIGIN, BIG_SIZE);
                imageCacheMap.put("big" + boarID, bigBoarImage);

                GraphicsUtil.drawImage(mediumBigBoarGraphics, filePath, ORIGIN, MEDIUM_BIG_SIZE);
                imageCacheMap.put("mediumBig" + boarID, mediumBigBoarImage);

                GraphicsUtil.drawImage(mediumBoarGraphics, filePath, ORIGIN, MEDIUM_SIZE);
                imageCacheMap.put("medium" + boarID, mediumBoarImage);
            } catch (Exception exception) {
                log.error("Failed to generate cache image for %s".formatted(boarID), exception);
                System.exit(-1);
            }
        }

        log.info("Successfully loaded all boar images into cache");
    }

    private static void loadBorders() {
        String rarityBorderPath = pathConfig.getMegaMenuAssets() + pathConfig.getRarityBorder();

        log.info("Attempting to load rarity borders into cache");

        for (String rarityID : config.getRarityConfigs().keySet()) {
            String color = config.getColorConfig().get(rarityID);

            BufferedImage rarityMediumBorderImage = new BufferedImage(
                MEDIUM_SIZE[0], MEDIUM_SIZE[1], BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D rarityMediumBorderG2D = rarityMediumBorderImage.createGraphics();

            BufferedImage rarityMediumBigBorderImage = new BufferedImage(
                MEDIUM_BIG_SIZE[0], MEDIUM_BIG_SIZE[1], BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D rarityMediumBigBorderG2D = rarityMediumBigBorderImage.createGraphics();

            try {
                GraphicsUtil.drawRect(rarityMediumBorderG2D, ORIGIN, MEDIUM_SIZE, color);
                rarityMediumBorderG2D.setComposite(AlphaComposite.DstIn);
                GraphicsUtil.drawImage(rarityMediumBorderG2D, rarityBorderPath, ORIGIN, MEDIUM_SIZE);
                rarityMediumBorderG2D.setComposite(AlphaComposite.SrcIn);
                imageCacheMap.put("border" + rarityID, rarityMediumBorderImage);

                GraphicsUtil.drawRect(rarityMediumBigBorderG2D, ORIGIN, MEDIUM_BIG_SIZE, color);
                rarityMediumBigBorderG2D.setComposite(AlphaComposite.DstIn);
                GraphicsUtil.drawImage(rarityMediumBigBorderG2D, rarityBorderPath, ORIGIN, MEDIUM_BIG_SIZE);
                rarityMediumBigBorderG2D.setComposite(AlphaComposite.SrcIn);
                imageCacheMap.put("borderMediumBig" + rarityID, rarityMediumBigBorderImage);
            } catch (Exception exception) {
                log.error("Failed to generate cache image for %s border".formatted(rarityID), exception);
                System.exit(-1);
            }
        }

        log.info("Successfully loaded all rarity borders into cache");
    }
}
