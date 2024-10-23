package dev.boarbot.util.generators;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.interactives.boar.market.MarketView;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class MarketImageGenerator extends ImageGenerator {
    private static final int[] IMAGE_SIZE = {1920, 1403};

    private static final int RARITIES_LEFT_X = 486;
    private static final int RARITIES_RIGHT_X = 1434;
    private static final int RARITIES_START_Y = 373;
    private static final int RARITIES_SPACING_Y = 190;

    private static final int ITEMS_PER_PAGE = 15;
    private static final int ITEMS_PER_ROW = 5;
    private static final int ITEMS_START_X = 25;
    private static final int ITEMS_START_Y = 266;
    private static final int AMOUNT_LEFT_X_OFFSET = 15;
    private static final int AMOUNT_RIGHT_X_OFFSET = 9;
    private static final int AMOUNT_TOP_Y_OFFSET = 49;
    private static final int AMOUNT_BOT_Y_OFFSET = 10;
    private static final int AMOUNT_BOX_WIDTH_OFFSET = 30;
    private static final int AMOUNT_BOX_HEIGHT = 64;

    private static final int[] FOCUSED_ITEM_POS = {640, 449};
    private static final int[] FOCUSED_RARITY_POS = {960, 399};
    private static final int[] FOCUSED_NAME_POS = {960, 1192};
    private static final int FOCUSED_TEXT_LEFT_X = 323;
    private static final int FOCUSED_TEXT_RIGHT_X = 1603;
    private static final int FOCUSED_TEXT_Y = 670;
    private static final int VALUE_Y_OFFSET = 78;
    private static final int LABEL_Y_SPACING = 198;

    private final int page;
    private final MarketView curView;
    private final List<String> itemIDs;
    private final String focusedID;

    public MarketImageGenerator(int page, MarketView curView, List<String> itemIDs, String focusedID) {
        this.page = page;
        this.curView = curView;
        this.itemIDs = itemIDs;
        this.focusedID = focusedID;
    }

    @Override
    public ImageGenerator generate() throws IOException, URISyntaxException {
        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        this.textDrawer = new TextDrawer(g2d, "", ORIGIN, Align.CENTER, COLORS.get("font"), NUMS.getFontSmallMedium());

        switch (this.curView) {
            case OVERVIEW -> drawOverview(g2d);
            case RARITIES -> drawRarities(g2d);
            case ITEMS -> drawItems(g2d);
            case FOCUSED -> drawFocused(g2d);
        }

        return this;
    }

    private void drawOverview(Graphics2D g2d) throws IOException, URISyntaxException {
        GraphicsUtil.drawImage(g2d, ResourceUtil.marketOverviewUnderlayPath, ORIGIN);
    }

    private void drawRarities(Graphics2D g2d) throws IOException, URISyntaxException {
        GraphicsUtil.drawImage(g2d, ResourceUtil.marketRaritiesUnderlayPath, ORIGIN);

        int curX;
        int curY = RARITIES_START_Y;
        int i = 0;

        this.textDrawer.setFontSize(NUMS.getFontBig());
        this.textDrawer.setAlign(Align.CENTER);

        for (String rarityID : RARITIES.keySet()) {
            RarityConfig rarity = RARITIES.get(rarityID);

            if (rarity.getAvgClones() == 0) {
                continue;
            }

            if (i % 2 == 0) {
                curX = RARITIES_LEFT_X;
            } else {
                curX = RARITIES_RIGHT_X;
            }

            this.textDrawer.setPos(new int[] {curX, curY});
            this.textDrawer.setText(rarity.getName() + " " + STRS.getMainItemPluralName());
            this.textDrawer.setColorVal(COLORS.get(rarityID));
            this.textDrawer.drawText();

            i++;

            if (i % 2 == 0) {
                curY += RARITIES_SPACING_Y;
            }
        }
    }

    private void drawItems(Graphics2D g2d) throws IOException, URISyntaxException {
        GraphicsUtil.drawImage(g2d, ResourceUtil.marketItemsUnderlayPath, ORIGIN);

        int border = NUMS.getBorder();
        int[] itemImageSize = NUMS.getMediumBoarSize();

        int lastItemIndex = Math.min((this.page+1)*ITEMS_PER_PAGE, this.itemIDs.size());

        for (int i=this.page*ITEMS_PER_PAGE; i<lastItemIndex; i++) {
            String itemID = this.itemIDs.get(i);
            String colorKey = POWS.containsKey(itemID) ? "powerup" : BoarUtil.findRarityKey(itemID);
            int relativeIndex = i - this.page*ITEMS_PER_PAGE;

            int[] itemPos = {
                ITEMS_START_X + (relativeIndex % ITEMS_PER_ROW) * (itemImageSize[0] + border),
                ITEMS_START_Y + (relativeIndex / ITEMS_PER_ROW) * (itemImageSize[1] + border)
            };

            String buyStr = shortenValue(MarketInteractive.cachedMarketData.get(itemID).buyPrice());
            String sellStr = shortenValue(MarketInteractive.cachedMarketData.get(itemID).sellPrice());

            this.textDrawer.setFontSize(NUMS.getFontMedium());

            FontMetrics fm = g2d.getFontMetrics();
            int[] buyRectSize = new int[] {
                fm.stringWidth(buyStr) + AMOUNT_BOX_WIDTH_OFFSET + border, AMOUNT_BOX_HEIGHT + border
            };
            int[] sellRectSize = new int[] {
                fm.stringWidth(sellStr) + AMOUNT_BOX_WIDTH_OFFSET + border, AMOUNT_BOX_HEIGHT + border
            };

            int[] buyPos = new int[] {itemPos[0] + AMOUNT_LEFT_X_OFFSET, itemPos[1] + AMOUNT_TOP_Y_OFFSET};
            int[] sellPos = new int[] {
                itemPos[0] + itemImageSize[0] - AMOUNT_RIGHT_X_OFFSET,
                itemPos[1] + itemImageSize[1] - AMOUNT_BOT_Y_OFFSET
            };

            BufferedImage itemImage = BoarBotApp.getBot().getImageCacheMap().get("medium" + itemID);
            g2d.drawImage(itemImage, itemPos[0], itemPos[1], null);

            BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("border" + colorKey);
            g2d.drawImage(rarityBorderImage, itemPos[0], itemPos[1], null);

            g2d.setPaint(Color.decode(COLORS.get("dark")));

            g2d.fill(new RoundRectangle2D.Double(
                itemPos[0]-border,
                itemPos[1]-border,
                buyRectSize[0],
                buyRectSize[1],
                NUMS.getBorder() * 2,
                NUMS.getBorder() * 2
            ));

            g2d.fill(new RoundRectangle2D.Double(
                itemPos[0]+itemImageSize[0]+border-sellRectSize[0],
                itemPos[1]+itemImageSize[1]+border-sellRectSize[1],
                sellRectSize[0],
                sellRectSize[1],
                NUMS.getBorder() * 2,
                NUMS.getBorder() * 2
            ));

            this.textDrawer.setText(buyStr);
            this.textDrawer.setPos(buyPos);
            this.textDrawer.setAlign(Align.LEFT);
            this.textDrawer.setColorVal(COLORS.get("green"));
            this.textDrawer.drawText();

            this.textDrawer.setText(sellStr);
            this.textDrawer.setPos(sellPos);
            this.textDrawer.setAlign(Align.RIGHT);
            this.textDrawer.setColorVal(COLORS.get("error"));
            this.textDrawer.drawText();
        }
    }

    private void drawFocused(Graphics2D g2d) throws IOException, URISyntaxException {
        GraphicsUtil.drawImage(g2d, ResourceUtil.marketFocusedUnderlayPath, ORIGIN);

        boolean isPowerup = POWS.containsKey(this.focusedID);
        String colorKey = isPowerup ? "powerup" : BoarUtil.findRarityKey(this.focusedID);
        String rarityStr = isPowerup
            ? "<>" + colorKey + "<>" + "POWERUP"
            : "<>" + colorKey + "<>" + RARITIES.get(colorKey).getName().toUpperCase();
        String itemName = isPowerup
            ? POWS.get(this.focusedID).getName()
            : BOARS.get(this.focusedID).getName();
        int targetStock = isPowerup
            ? POWS.get(this.focusedID).getTargetStock()
            : RARITIES.get(colorKey).getTargetStock();

        BufferedImage itemImage = BoarBotApp.getBot().getImageCacheMap().get("mediumBig" + this.focusedID);
        g2d.drawImage(itemImage, FOCUSED_ITEM_POS[0], FOCUSED_ITEM_POS[1], null);

        BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("borderMediumBig" + colorKey);
        g2d.drawImage(rarityBorderImage, FOCUSED_ITEM_POS[0], FOCUSED_ITEM_POS[1], null);

        int[] buyLabelPos = {FOCUSED_TEXT_LEFT_X, FOCUSED_TEXT_Y};
        String buyStr = "<>bucks<>$%,d".formatted(MarketInteractive.cachedMarketData.get(this.focusedID).buyPrice());
        int[] buyPos = {FOCUSED_TEXT_LEFT_X, buyLabelPos[1] + VALUE_Y_OFFSET};

        int[] sellLabelPos = {FOCUSED_TEXT_LEFT_X, buyLabelPos[1] + LABEL_Y_SPACING};
        String sellStr = "<>bucks<>$%,d".formatted(MarketInteractive.cachedMarketData.get(this.focusedID).sellPrice());
        int[] sellPos = {FOCUSED_TEXT_LEFT_X, sellLabelPos[1] + VALUE_Y_OFFSET};

        int[] targetLabelPos = {FOCUSED_TEXT_RIGHT_X, FOCUSED_TEXT_Y};
        String targetStr = "%,d".formatted(targetStock);
        int[] targetPos = {FOCUSED_TEXT_RIGHT_X, targetLabelPos[1] + VALUE_Y_OFFSET};

        int[] stockLabelPos = {FOCUSED_TEXT_RIGHT_X, targetLabelPos[1] + LABEL_Y_SPACING};
        String stockStr = "%,d".formatted(MarketInteractive.cachedMarketData.get(this.focusedID).stock());
        int[] stockPos = {FOCUSED_TEXT_RIGHT_X, stockLabelPos[1] + VALUE_Y_OFFSET};

        this.textDrawer.setText(rarityStr);
        this.textDrawer.setPos(FOCUSED_RARITY_POS);
        this.textDrawer.setAlign(Align.CENTER);
        this.textDrawer.setFontSize(NUMS.getFontBig());
        this.textDrawer.drawText();

        this.textDrawer.setText(itemName);
        this.textDrawer.setPos(FOCUSED_NAME_POS);
        this.textDrawer.drawText();

        TextUtil.drawLabel(this.textDrawer, STRS.getMarketBuyLabel(), buyLabelPos);
        TextUtil.drawValue(this.textDrawer, buyStr, buyPos);

        TextUtil.drawLabel(this.textDrawer, STRS.getMarketSellLabel(), sellLabelPos);
        TextUtil.drawValue(this.textDrawer, sellStr, sellPos);

        TextUtil.drawLabel(this.textDrawer, STRS.getMarketTargetStockLabel(), targetLabelPos);
        TextUtil.drawValue(this.textDrawer, targetStr, targetPos);

        TextUtil.drawLabel(this.textDrawer, STRS.getMarketStockLabel(), stockLabelPos);
        TextUtil.drawValue(this.textDrawer, stockStr, stockPos);
    }

    private String shortenValue(long value) {
        String valStr = Long.toString(value);

        if (value / 1000000000000000L >= 1) {
            valStr = value/1000000000000000L + "q";
        } else if (value / 1000000000000L >= 1) {
            valStr = value/1000000000000L + "t";
        } else if (value / 1000000000 >= 1) {
            valStr = value/1000000000 + "b";
        } else if (value / 1000000 >= 1) {
            valStr = value/1000000 + "m";
        } else if (value / 1000 >= 1) {
            valStr = value/1000 + "k";
        }

        return valStr;
    }
}
