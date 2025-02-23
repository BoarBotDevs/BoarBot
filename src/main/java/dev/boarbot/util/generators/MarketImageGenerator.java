package dev.boarbot.util.generators;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.interactives.boar.market.MarketView;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.market.MarketData;
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
import java.text.DecimalFormat;
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
    private static final int AMOUNT_TOP_Y_OFFSET = 49;
    private static final int AMOUNT_BOX_WIDTH_OFFSET = 30;
    private static final int AMOUNT_BOX_HEIGHT = 64;

    private static final int[] FOCUSED_ITEM_POS = {174, 455};
    private static final int FOCUSED_TEXT_LEFT_X = 497;
    private static final int FOCUSED_RARITY_Y = 350;
    private static final int FOCUSED_NAME_Y = 415;
    private static final int FOCUSED_PRICE_LABEL_Y = 1174;
    private static final int FOCUSED_PRICE_VALUE_Y_OFFSET = 78;
    private static final int FOCUSED_TEXT_RIGHT_X = 1371;
    private static final int FOCUSED_LIST_TITLE_Y = 474;
    private static final int FOCUSED_LIST_START_Y = 553;
    private static final int FOCUSED_LIST_Y_OFFSET = 64;

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

            String priceStr = MarketInteractive.cachedMarketData.get(itemID).isEmpty()
                ? STRS.getUnavailable()
                : "$" + shortenValue(MarketInteractive.cachedMarketData.get(itemID).getFirst().price(), false);
            String priceColor = MarketInteractive.cachedMarketData.get(itemID).isEmpty() ? "silver" : "bucks";

            this.textDrawer.setFontSize(NUMS.getFontMedium());

            FontMetrics fm = g2d.getFontMetrics();
            int[] buyRectSize = new int[] {
                fm.stringWidth(priceStr) + AMOUNT_BOX_WIDTH_OFFSET + border, AMOUNT_BOX_HEIGHT + border
            };

            int[] buyPos = new int[] {itemPos[0] + AMOUNT_LEFT_X_OFFSET, itemPos[1] + AMOUNT_TOP_Y_OFFSET};

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

            this.textDrawer.setText(priceStr);
            this.textDrawer.setPos(buyPos);
            this.textDrawer.setAlign(Align.LEFT);
            this.textDrawer.setColorVal(COLORS.get(priceColor));
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

        BufferedImage itemImage = BoarBotApp.getBot().getImageCacheMap().get("mediumBig" + this.focusedID);
        g2d.drawImage(itemImage, FOCUSED_ITEM_POS[0], FOCUSED_ITEM_POS[1], null);

        BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("borderMediumBig" + colorKey);
        g2d.drawImage(rarityBorderImage, FOCUSED_ITEM_POS[0], FOCUSED_ITEM_POS[1], null);

        int[] rarityPos = {FOCUSED_TEXT_LEFT_X, FOCUSED_RARITY_Y};
        int[] namePos = {FOCUSED_TEXT_LEFT_X, FOCUSED_NAME_Y};

        int[] priceLabelPos = {FOCUSED_TEXT_LEFT_X, FOCUSED_PRICE_LABEL_Y};
        String priceStr = MarketInteractive.cachedMarketData.get(this.focusedID).isEmpty()
            ? "<>silver<>" + STRS.getUnavailable()
            : "<>bucks<>$" + shortenValue(
                MarketInteractive.cachedMarketData.get(this.focusedID).getFirst().price(), true
            );
        int[] pricePos = {FOCUSED_TEXT_LEFT_X, priceLabelPos[1] + FOCUSED_PRICE_VALUE_Y_OFFSET};

        int[] listTitlePos = {FOCUSED_TEXT_RIGHT_X, FOCUSED_LIST_TITLE_Y};
        int[] curListPos = {FOCUSED_TEXT_RIGHT_X, FOCUSED_LIST_START_Y};
        long curPrice = 0;
        long curAmount = 0;
        int marketIndex = 0;

        this.textDrawer.setAlign(Align.CENTER);
        this.textDrawer.setFontSize(NUMS.getFontMedium());
        this.textDrawer.setColorVal(COLORS.get("silver"));

        for (int i=0; i<10; i++) {
            this.textDrawer.setPos(curListPos);

            if (MarketInteractive.cachedMarketData.get(this.focusedID).size() <= marketIndex) {
                this.textDrawer.setText(curPrice == 0
                    ? STRS.getMarketBlankListValue()
                    : STRS.getMarketListValue().formatted(shortenValue(curPrice, true), curAmount)
                );
                this.textDrawer.drawText();

                curListPos[1] += FOCUSED_LIST_Y_OFFSET;
                curPrice = 0;
                curAmount = 0;

                continue;
            }

            MarketData marketData = MarketInteractive.cachedMarketData.get(this.focusedID).get(marketIndex);

            if (curPrice != 0 && curPrice != marketData.price()) {
                this.textDrawer.setText(STRS.getMarketListValue().formatted(shortenValue(curPrice, true), curAmount));
                this.textDrawer.drawText();

                curListPos[1] += FOCUSED_LIST_Y_OFFSET;
                curPrice = marketData.price();
                curAmount = marketData.amount();
            } else if (curPrice == 0) {
                curPrice = marketData.price();
                curAmount = marketData.amount();
                i--;
            } else {
                curAmount += marketData.amount();
                i--;
            }

            marketIndex++;
        }

        this.textDrawer.setText(STRS.getMarketListTitle());
        this.textDrawer.setPos(listTitlePos);
        this.textDrawer.setAlign(Align.CENTER);
        this.textDrawer.setFontSize(NUMS.getFontBig());
        this.textDrawer.setColorVal(COLORS.get("font"));
        this.textDrawer.drawText();

        this.textDrawer.setText(rarityStr);
        this.textDrawer.setPos(rarityPos);
        this.textDrawer.setAlign(Align.CENTER);
        this.textDrawer.setFontSize(NUMS.getFontMedium());
        this.textDrawer.drawText();

        this.textDrawer.setText(itemName);
        this.textDrawer.setPos(namePos);
        this.textDrawer.drawText();

        TextUtil.drawLabel(this.textDrawer, STRS.getMarketPriceLabel(), priceLabelPos);
        TextUtil.drawValue(this.textDrawer, priceStr, pricePos);
    }

    private String shortenValue(long value, boolean focused) {
        DecimalFormat decFormat = new DecimalFormat("#,###.#");
        String valStr = decFormat.format(value);

        if (value >= 1000000000000000L) {
            valStr = decFormat.format((value / 1000000000000000.0)) + "q";
        } else if (value >= 1000000000000L) {
            valStr = decFormat.format((value / 1000000000000.0)) + "t";
        } else if (value >= 1000000000) {
            valStr = decFormat.format((value / 1000000000.0)) + "b";
        } else if (value >= 1000000) {
            valStr = decFormat.format((value / 1000000.0)) + "m";
        } else if (value >= 1000 && !focused) {
            valStr = decFormat.format((value / 1000.0)) + "k";
        }

        return valStr;
    }
}
