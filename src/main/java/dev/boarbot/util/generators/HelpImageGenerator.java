package dev.boarbot.util.generators;

import dev.boarbot.interactives.boar.help.HelpView;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

public class HelpImageGenerator extends ImageGenerator {
    private static final int[] IMAGE_SIZE = {1920, 1403};
    private static final int WIDTH = 825;
    private static final int LEFT_X = 80;
    private static final int RIGHT_X = 1000;
    private static final int HEADER_Y = 314;
    private static final int PARA_Y = 790;

    private final HelpView curView;
    private final int page;

    private String headerLeft;
    private String headerRight;
    private String paraLeft;
    private String paraRight;

    public HelpImageGenerator(HelpView curView, int page) {
        this.curView = curView;
        this.page = page;
    }

    @Override
    public ImageGenerator generate() throws IOException, URISyntaxException {
        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.helpUnderlayPath, ORIGIN);

        this.textDrawer = new TextDrawer(
            g2d, "", ORIGIN, Align.LEFT, COLORS.get("font"), NUMS.getFontMedium(), WIDTH, true
        );

        switch (this.curView) {
            case GENERAL -> setGeneralText();
            case BOARS -> setBoarsText();
            case BADGES -> setBadgesText();
            case POWERUPS -> setPowerupsText();
        }

        drawHeader(this.headerLeft, new int[] {LEFT_X, HEADER_Y});
        drawHeader(this.headerRight, new int[] {RIGHT_X, HEADER_Y});
        drawPara(this.paraLeft, new int[] {LEFT_X, PARA_Y});
        drawPara(this.paraRight, new int[] {RIGHT_X, PARA_Y});

        return this;
    }

    private void setGeneralText() {
        if (this.page == 0) {
            this.headerLeft = STRS.getHelpGen1HL();
            this.headerRight = STRS.getHelpGen1HR();
            this.paraLeft = String.join(" ", STRS.getHelpGen1PL());
            this.paraRight = String.join(" ", STRS.getHelpGen1PR());
        } else {
            this.headerLeft = STRS.getHelpGen2HL();
            this.headerRight = STRS.getHelpGen2HR();
            this.paraLeft = String.join(" ", STRS.getHelpGen2PL());
            this.paraRight = String.join(" ", STRS.getHelpGen2PR());
        }
    }

    private void setBoarsText() {
        if (this.page == 0) {
            this.headerLeft = STRS.getHelpBoar1HL();
            this.headerRight = STRS.getHelpBoar1HR();
            this.paraLeft = String.join(" ", STRS.getHelpBoar1PL());
            this.paraRight = String.join(" ", STRS.getHelpBoar1PR());
        } else {
            this.headerLeft = STRS.getHelpBoar2HL();
            this.headerRight = STRS.getHelpBoar2HR();
            this.paraLeft = String.join(" ", STRS.getHelpBoar2PL());
            this.paraRight = String.join(" ", STRS.getHelpBoar2PR());
        }
    }

    private void setBadgesText() {
        this.headerLeft = STRS.getHelpBadge1HL();
        this.paraLeft = String.join(" ", STRS.getHelpBadge1PL());
        this.paraRight = String.join(" ", STRS.getHelpBadge1PR());
    }

    private void setPowerupsText() {
        if (this.page == 0) {
            this.headerLeft = STRS.getHelpPow1HL();
            this.headerRight = STRS.getHelpPow1HR();
            this.paraLeft = String.join(" ", STRS.getHelpPow1PL());
            this.paraRight = String.join(" ", STRS.getHelpPow1PR());
        } else if (this.page == 1) {
            this.headerLeft = STRS.getHelpPow2HL();
            this.headerRight = STRS.getHelpPow2HR();
            this.paraLeft = String.join(" ", STRS.getHelpPow2PL());
            this.paraRight = String.join(" ", STRS.getHelpPow2PR());
        } else {
            this.headerLeft = STRS.getHelpPow3HL();
            this.headerRight = STRS.getHelpPow3HR();
            this.paraLeft = String.join(" ", STRS.getHelpPow3PL());
            this.paraRight = String.join(" ", STRS.getHelpPow3PR());
        }
    }

    private void drawHeader(String header, int[] pos) {
        if (header == null) {
            return;
        }

        this.textDrawer.setFontSize(NUMS.getFontMedium());
        this.textDrawer.setText(header);
        this.textDrawer.setPos(pos);
        this.textDrawer.drawText();
    }

    private void drawPara(String para, int[] pos) {
        if (para == null) {
            return;
        }

        this.textDrawer.setFontSize(NUMS.getFontSmallest());
        this.textDrawer.setText(para);
        this.textDrawer.setPos(pos);
        this.textDrawer.drawText();
    }
}
