package dev.boarbot.util.generators.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class BadgesImageGenerator extends MegaMenuGenerator {
    public static final int BOARS_PER_PAGE = 15;

    private static final int MAX_BOARS = 9999999;
    private static final int[] ORIGIN = {0, 0};
    private static final int START_X = 25;
    private static final int START_Y = 266;
    private static final int NUM_COLS = 5;
    private static final int AMOUNT_X_OFFSET = 15;
    private static final int AMOUNT_Y_OFFSET = 49;
    private static final int AMOUNT_BOX_WIDTH_OFFSET = 30;
    private static final int AMOUNT_BOX_HEIGHT = 59;

    private final Map<String, BoarInfo> boarInfos;

    public BadgesImageGenerator(
        int page, BoarUser boarUser, List<String> badgeIDs, String firstJoinedDate, Map<String, BoarInfo> boarInfos
    ) {
        super(page, boarUser, badgeIDs, firstJoinedDate);
        this.boarInfos = boarInfos;
    }

    public FileUpload generate() throws IOException, URISyntaxException {
        int border = nums.getBorder();
        int[] boarImageSize = this.nums.getMediumBoarSize();

        String underlayPath = this.pathConfig.getMegaMenuAssets() + this.pathConfig.getCollUnderlay();

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN, IMAGE_SIZE);

        String[] boarIDs = this.boarInfos.keySet().toArray(new String[0]);
        int lastBoarIndex = Math.min((this.page+1)*BOARS_PER_PAGE, boarIDs.length);

        for (int i=this.page*BOARS_PER_PAGE; i<lastBoarIndex; i++) {
            String boarID = boarIDs[i];
            int relativeIndex = i - this.page*BOARS_PER_PAGE;

            int[] boarPos = {
                    START_X + (relativeIndex % NUM_COLS) * (boarImageSize[0] + this.nums.getBorder()),
                    START_Y + (relativeIndex / NUM_COLS) * (boarImageSize[1] + this.nums.getBorder())
            };

            int[] amountPos = new int[] {boarPos[0] + AMOUNT_X_OFFSET, boarPos[1] + AMOUNT_Y_OFFSET};
            String amount = "%,d".formatted(Math.min(this.boarInfos.get(boarID).amount(), MAX_BOARS));
            TextDrawer textDrawer = new TextDrawer(
                    g2d, amount, amountPos, Align.LEFT, this.colorConfig.get("font"), this.nums.getFontMedium()
            );
            FontMetrics fm = g2d.getFontMetrics();
            int[] rectangleSize = new int[] {
                    fm.stringWidth(amount) + AMOUNT_BOX_WIDTH_OFFSET + border,
                    AMOUNT_BOX_HEIGHT + border
            };

            BufferedImage boarImage = BoarBotApp.getBot().getImageCacheMap().get("medium" + boarID);
            g2d.drawImage(boarImage, boarPos[0], boarPos[1], null);

            BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get(
                    "border" + this.boarInfos.get(boarID).rarityID()
            );
            g2d.drawImage(rarityBorderImage, boarPos[0], boarPos[1], null);

            g2d.setPaint(Color.decode(this.colorConfig.get("dark")));
            g2d.fill(new RoundRectangle2D.Double(
                    boarPos[0]-border,
                    boarPos[1]-border,
                    rectangleSize[0],
                    rectangleSize[1],
                    this.nums.getBorder() * 2,
                    this.nums.getBorder() * 2
            ));
            textDrawer.drawText();
        }

        this.drawTopInfo();
        return this.getFileUpload();
    }
}
