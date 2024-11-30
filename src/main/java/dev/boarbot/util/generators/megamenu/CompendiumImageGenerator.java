package dev.boarbot.util.generators.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.resource.ResourceUtil;
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public class CompendiumImageGenerator extends MegaMenuGenerator {
    private static final int[] FAV_POS = {130, 472};
    private static final int[] LEFT_START_POS = {484, 392};
    private static final int VALUE_Y_OFFSET = 78;
    private static final int LABEL_Y_SPACING = 198;
    private static final int[] RARITY_POS = {1431, 326};
    private static final int[] NAME_POS = {1431, 392};
    private static final int[] BOAR_POS = {1108, 455};
    private static final int RIGHT_WIDTH = 800;
    private static final int[] DESCRIPTION_POS = {1431, 1253};

    private final String boarID;
    private final BoarInfo boarInfo;
    private final boolean isFavorite;

    public CompendiumImageGenerator(
        int page,
        BoarUser boarUser,
        List<BadgeData> badges,
        String firstJoinedDate,
        boolean isFavorite,
        Map.Entry<String, BoarInfo> boarEntry
    ) {
        super(page, boarUser, badges, firstJoinedDate);
        this.boarID = boarEntry.getKey();
        this.boarInfo = boarEntry.getValue();
        this.isFavorite = isFavorite;
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        int smallestFont = NUMS.getFontSmallest();
        int mediumFont = NUMS.getFontMedium();

        BoarItemConfig boar = BOARS.get(this.boarID);

        int[] amountPos = {LEFT_START_POS[0], LEFT_START_POS[1] + VALUE_Y_OFFSET};

        int[] oldestLabelPos = {LEFT_START_POS[0], LEFT_START_POS[1] + LABEL_Y_SPACING};
        int[] oldestPos = {oldestLabelPos[0], oldestLabelPos[1] + VALUE_Y_OFFSET};

        int[] newestLabelPos = {oldestLabelPos[0], oldestLabelPos[1] + LABEL_Y_SPACING};
        int[] newestPos = {newestLabelPos[0], newestLabelPos[1] + VALUE_Y_OFFSET};

        int[] classLabelPos = {newestLabelPos[0], newestLabelPos[1] + LABEL_Y_SPACING};
        int[] classPos = {classLabelPos[0], classLabelPos[1] + VALUE_Y_OFFSET};

        int[] updateLabelPos = {classLabelPos[0], classLabelPos[1] + LABEL_Y_SPACING};
        int[] updatePos = {updateLabelPos[0], updateLabelPos[1] + VALUE_Y_OFFSET};

        String rarityKey = BoarUtil.findRarityKey(this.boarID);
        String rarityName = RARITIES.get(rarityKey).getName();

        String firstObtained = this.boarInfo.getFirstObtained() > -1
            ? Instant.ofEpochMilli(this.boarInfo.getFirstObtained())
                .atOffset(ZoneOffset.UTC)
                .format(TimeUtil.getDateFormatter())
            : STRS.getUnavailable();
        String lastObtained = this.boarInfo.getLastObtained() > -1
            ? Instant.ofEpochMilli(this.boarInfo.getLastObtained())
                .atOffset(ZoneOffset.UTC)
                .format(TimeUtil.getDateFormatter())
            : STRS.getUnavailable();

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.compUnderlayPath, ORIGIN, IMAGE_SIZE);

        if (this.isFavorite) {
            GraphicsUtil.drawImage(g2d, ResourceUtil.favoritePath, FAV_POS);
        }

        this.textDrawer = new TextDrawer(
            g2d, STRS.getCompAmountLabel(), LEFT_START_POS, Align.CENTER, COLORS.get("font"), mediumFont
        );
        this.textDrawer.drawText();

        this.drawValue("%,d".formatted(this.boarInfo.getAmount()), amountPos);

        this.drawLabel(STRS.getCompOldestLabel(), oldestLabelPos);
        this.drawValue(firstObtained, oldestPos);

        this.drawLabel(STRS.getCompNewestLabel(), newestLabelPos);
        this.drawValue(lastObtained, newestPos);

        this.drawLabel(STRS.getCompSpeciesLabel(), classLabelPos);
        this.drawValue(this.boarInfo.getAmount() > 0
            ? boar.getClassification()
            : STRS.getCompNoSpecies(),
        classPos);

        this.drawLabel(STRS.getCompUpdateLabel(), updateLabelPos);
        this.drawValue(boar.getUpdate(), updatePos);

        this.textDrawer.setText(rarityName.toUpperCase());
        this.textDrawer.setPos(RARITY_POS);
        this.textDrawer.setFontSize(mediumFont);
        this.textDrawer.setColorVal(COLORS.get(rarityKey));
        this.textDrawer.setWidth(RIGHT_WIDTH);
        this.textDrawer.drawText();

        this.textDrawer.setText(boar.getName());
        this.textDrawer.setPos(NAME_POS);
        this.textDrawer.setColorVal(COLORS.get("font"));
        this.textDrawer.drawText();

        if (this.boarInfo.getAmount() > 0) {
            BufferedImage boarImage = BoarBotApp.getBot().getImageCacheMap().get("mediumBig" + this.boarID);
            g2d.drawImage(boarImage, BOAR_POS[0], BOAR_POS[1], null);
        }

        BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("borderMediumBig" + rarityKey);
        g2d.drawImage(rarityBorderImage, BOAR_POS[0], BOAR_POS[1], null);

        this.textDrawer.setText(this.boarInfo.getAmount() > 0
            ? boar.getDescription()
            : "Description Unknown"
        );
        this.textDrawer.setPos(DESCRIPTION_POS);
        this.textDrawer.setFontSize(smallestFont);
        this.textDrawer.setWrap(true);
        this.textDrawer.drawText();

        this.drawTopInfo();
        return this;
    }

    private void drawLabel(String text, int[] pos) {
        int mediumFont = NUMS.getFontMedium();

        this.textDrawer.setText(text);
        this.textDrawer.setPos(pos);
        this.textDrawer.setFontSize(mediumFont);
        this.textDrawer.setColorVal(COLORS.get("font"));
        this.textDrawer.drawText();
    }

    private void drawValue(String text, int[] pos) {
        int bigFont = NUMS.getFontBig();

        this.textDrawer.setText(text);
        this.textDrawer.setPos(pos);
        this.textDrawer.setFontSize(bigFont);
        this.textDrawer.setColorVal(COLORS.get("silver"));
        this.textDrawer.drawText();
    }
}
