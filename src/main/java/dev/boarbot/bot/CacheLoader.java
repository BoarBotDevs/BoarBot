package dev.boarbot.bot;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;

import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.interactives.boar.TopInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.top.TopData;
import dev.boarbot.util.data.top.TopDataUtil;
import dev.boarbot.util.data.top.TopType;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.resource.ResourceUtil;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class CacheLoader implements Configured {
    private final static Map<String, BufferedImage> imageCacheMap = BoarBotApp.getBot().getImageCacheMap();

    private final static int[] ORIGIN = {0, 0};
    private final static int[] LARGE_SIZE = NUMS.getLargeBoarSize();
    private final static int[] BIG_SIZE = NUMS.getBigBoarSize();
    private final static int[] MEDIUM_BIG_SIZE = NUMS.getMediumBigBoarSize();
    private final static int[] MEDIUM_SIZE = NUMS.getMediumBoarSize();

    static void loadCache() {
        loadBoars();
        loadBorders();
        reloadTop();
    }

    private static void loadBoars() {
        Log.debug(CacheLoader.class, "Attempting to load boar images into cache...");

        for (String boarID : BOARS.keySet()) {
            try {
                BoarItemConfig boarInfo = BOARS.get(boarID);

                if (boarInfo.getFile().isEmpty()) {
                    throw new IllegalArgumentException("Failed to find file");
                }

                String filePath = boarInfo.getStaticFile() != null
                    ? ResourceUtil.boarAssetsPath + boarInfo.getStaticFile()
                    : ResourceUtil.boarAssetsPath + boarInfo.getFile();

                if (filePath.endsWith(".gif")) {
                    throw new IllegalArgumentException("Animated file is missing a static version");
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
            } catch (IOException exception) {
                Log.error(CacheLoader.class, "Failed to read image file for %s".formatted(boarID), exception);
                System.exit(-1);
            } catch (URISyntaxException ignored) {}
        }

        Log.debug(CacheLoader.class, "Successfully loaded all boar images into cache");
    }

    private static void loadBorders() {
        Log.debug(CacheLoader.class, "Attempting to load rarity borders into cache...");

        for (String rarityID : RARITIES.keySet()) {
            String color = COLORS.get(rarityID);

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
                GraphicsUtil.drawImage(rarityMediumBorderG2D, ResourceUtil.rarityBorderPath, ORIGIN, MEDIUM_SIZE);
                rarityMediumBorderG2D.setComposite(AlphaComposite.SrcIn);
                imageCacheMap.put("border" + rarityID, rarityMediumBorderImage);

                GraphicsUtil.drawRect(rarityMediumBigBorderG2D, ORIGIN, MEDIUM_BIG_SIZE, color);
                rarityMediumBigBorderG2D.setComposite(AlphaComposite.DstIn);
                GraphicsUtil.drawImage(
                    rarityMediumBigBorderG2D, ResourceUtil.rarityBorderPath, ORIGIN, MEDIUM_BIG_SIZE
                );
                rarityMediumBigBorderG2D.setComposite(AlphaComposite.SrcIn);
                imageCacheMap.put("borderMediumBig" + rarityID, rarityMediumBigBorderImage);
            } catch (IOException exception) {
                Log.error(CacheLoader.class, "Failed to read image file for %s border".formatted(rarityID), exception);
                System.exit(-1);
            } catch (URISyntaxException ignored) {}
        }

        Log.debug(CacheLoader.class, "Successfully loaded all rarity borders into cache");
    }

    public synchronized static void reloadTop() {
        try (Connection connection = DataUtil.getConnection()) {
            Set<String> firstUsernames = new HashSet<>();
            Set<String> secondUsernames = new HashSet<>();
            Set<String> thirdUsernames = new HashSet<>();

            for (TopType topType : TopType.values()) {
                Map<String, TopData> board = TopDataUtil.getBoard(topType, connection);
                TopInteractive.cachedBoards.put(topType, board);

                List<String> usernameList = new ArrayList<>(board.keySet());
                TopInteractive.indexedBoards.put(topType, usernameList);

                if (!TopInteractive.indexedBoards.get(topType).isEmpty()) {
                    firstUsernames.add(TopInteractive.indexedBoards.get(topType).getFirst());
                }

                if (TopInteractive.indexedBoards.get(topType).size() > 1) {
                    secondUsernames.add(TopInteractive.indexedBoards.get(topType).get(1));
                }

                if (TopInteractive.indexedBoards.get(topType).size() > 2) {
                    thirdUsernames.add(TopInteractive.indexedBoards.get(topType).get(2));
                }
            }

            secondUsernames.removeAll(firstUsernames);
            thirdUsernames.removeAll(firstUsernames);
            thirdUsernames.removeAll(secondUsernames);

            TopDataUtil.setAthleteBadges(firstUsernames, secondUsernames, thirdUsernames, connection);
        } catch (SQLException exception) {
            Log.error(CacheLoader.class, "Failed to retrieve leaderboard data", exception);
        }
    }
}
