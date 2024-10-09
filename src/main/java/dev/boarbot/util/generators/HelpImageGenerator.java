package dev.boarbot.util.generators;

import dev.boarbot.bot.config.commands.ArgChoicesConfig;
import dev.boarbot.interactives.boar.help.HelpView;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HelpImageGenerator extends ImageGenerator {
    private static final int[] IMAGE_SIZE = {1920, 1403};
    private static final int WIDTH = 825;
    private static final int LEFT_X = 80;
    private static final int RIGHT_X = 1000;
    private static final int HEADER_Y = 314;
    private static final int PARA_Y = 790;
    private static final int[] INFO_POS = {960, 1320};
    private static final int INFO_SPACING_Y = 54;

    private final HelpView curView;
    private final int page;
    private final int maxPage;

    private String headerLeft;
    private String headerRight;
    private String paraLeft;
    private String paraRight;

    private static final Map<String, Integer> rarityBoarAmounts = new HashMap<>();

    static {
        for (String key : RARITIES.keySet()) {
            rarityBoarAmounts.put(key, BoarUtil.getNumRarityBoars(RARITIES.get(key)));
        }
    }

    public HelpImageGenerator(HelpView curView, int page, int maxPage) {
        this.curView = curView;
        this.page = page;
        this.maxPage = maxPage;
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

        this.textDrawer.setText(STRS.getHelpPageInfo().formatted(this.page+1, this.maxPage+1));
        this.textDrawer.setPos(INFO_POS);
        this.textDrawer.setColorVal(COLORS.get("font"));
        this.textDrawer.setFontSize(NUMS.getFontMedium());
        this.textDrawer.setWidth(-1);
        this.textDrawer.setAlign(Align.CENTER);
        this.textDrawer.drawText();

        this.textDrawer.setText(STRS.getHelpMenuInfo().formatted(this.getMenuName()));
        this.textDrawer.setPos(new int[] {INFO_POS[0], INFO_POS[1] + INFO_SPACING_Y});
        this.textDrawer.drawText();

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

            this.paraLeft = String.join(" ", STRS.getHelpBoar1PL()).formatted(
                BoarUtil.getTotalUniques(),
                rarityBoarAmounts.get("common"),
                rarityBoarAmounts.get("halloween"),
                rarityBoarAmounts.get("uncommon"),
                rarityBoarAmounts.get("christmas"),
                rarityBoarAmounts.get("rare"),
                rarityBoarAmounts.get("event"),
                rarityBoarAmounts.get("epic"),
                rarityBoarAmounts.get("legendary"),
                rarityBoarAmounts.get("mythic"),
                rarityBoarAmounts.get("divine"),
                rarityBoarAmounts.get("immaculate"),
                rarityBoarAmounts.get("entropic"),
                rarityBoarAmounts.get("truth"),
                rarityBoarAmounts.get("special")
            );

            this.paraRight = String.join(" ", STRS.getHelpBoar1PR());
        } else {
            this.headerLeft = STRS.getHelpBoar2HL();
            this.headerRight = STRS.getHelpBoar2HR();
            this.paraLeft = String.join(" ", STRS.getHelpBoar2PL());

            this.paraRight = String.join(" ", STRS.getHelpBoar2PR()).formatted(
                rarityBoarAmounts.get("halloween"),
                rarityBoarAmounts.get("christmas"),
                rarityBoarAmounts.get("event")
            );
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

    private String getMenuName() {
        ArgChoicesConfig<?>[] choices = CONFIG.getCommandConfig().get("main").getSubcommands().get("help")
            .getOptions()[0].getChoices();

        for (ArgChoicesConfig<?> choice : choices) {
            if (choice.getValue().equals(this.curView.toString())) {
                return choice.getName();
            }
        }

        return null;
    }
}
