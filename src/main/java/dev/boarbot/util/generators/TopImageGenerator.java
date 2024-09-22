package dev.boarbot.util.generators;

import dev.boarbot.bot.config.commands.ArgChoicesConfig;
import dev.boarbot.interactives.boar.TopInteractive;
import dev.boarbot.util.data.top.TopType;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

public class TopImageGenerator extends ImageGenerator {
    private static final int[] IMAGE_SIZE = {1920, 1403};
    private static final int ENTRIES_PER_PAGE = 44;
    private static final int LEFT_X = 478;
    private static final int RIGHT_X = 1443;
    private static final int START_Y = 259;
    private static final int SPACING_Y = 42;
    private static final int WIDTH = 450;
    private static final int[] INFO_POS = {960, 1320};
    private static final int INFO_SPACING_Y = 54;

    private final int page;
    private final TopType topType;
    private final int totalEntries;
    private final int totalPages;
    private final int userIndex;

    public TopImageGenerator(int page, TopType topType, Integer usernameIndex) {
        this.page = page;
        this.topType = topType;
        this.totalEntries = TopInteractive.indexedBoards.get(topType).size();
        this.totalPages = Math.max((this.totalEntries-1) / ENTRIES_PER_PAGE, 0);

        this.userIndex = usernameIndex == null
            ? -1
            : usernameIndex;
    }

    @Override
    public ImageGenerator generate() throws IOException, URISyntaxException {
        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.topUnderlayPath, ORIGIN);

        this.textDrawer = new TextDrawer(
            g2d, "", ORIGIN, Align.CENTER, COLORS.get("font"), NUMS.getFontSmallest(), WIDTH
        );

        int startIndex = this.page * ENTRIES_PER_PAGE;
        int endIndex = Math.min((this.page+1) * ENTRIES_PER_PAGE, this.totalEntries);

        int[] curPos = {LEFT_X, START_Y};

        for (int i=startIndex; i<endIndex; i++) {
            if (i != startIndex && i % 22 == 0) {
                curPos = new int[] {RIGHT_X, START_Y};
            }

            String username = TopInteractive.indexedBoards.get(this.topType).get(i);
            long value = TopInteractive.cachedBoards.get(this.topType).get(username).value();

            this.textDrawer.setText(STRS.getTopEntry().formatted(i+1, username, value));
            this.textDrawer.setPos(curPos);

            if (i == this.userIndex) {
                this.textDrawer.setColorVal(COLORS.get("green"));
            } else if (i == 0) {
                this.textDrawer.setColorVal(COLORS.get("gold"));
            } else if (i == 1) {
                this.textDrawer.setColorVal(COLORS.get("silver"));
            } else if (i == 2) {
                this.textDrawer.setColorVal(COLORS.get("bronze"));
            } else {
                this.textDrawer.setColorVal(COLORS.get("font"));
            }

            this.textDrawer.drawText();

            curPos[1] += SPACING_Y;
        }

        this.textDrawer.setText(STRS.getTopPageInfo().formatted(this.totalEntries, this.page+1, this.totalPages+1));
        this.textDrawer.setPos(INFO_POS);
        this.textDrawer.setColorVal(COLORS.get("font"));
        this.textDrawer.setFontSize(NUMS.getFontMedium());
        this.textDrawer.setWidth(-1);
        this.textDrawer.drawText();

        this.textDrawer.setText(STRS.getTopBoardInfo().formatted(this.getBoardName()));
        this.textDrawer.setPos(new int[] {INFO_POS[0], INFO_POS[1] + INFO_SPACING_Y});
        this.textDrawer.drawText();

        return this;
    }

    private String getBoardName() {
        ArgChoicesConfig<?>[] choices = CONFIG.getCommandConfig().get("main").getSubcommands().get("leaderboard")
            .getOptions()[0].getChoices();

        for (ArgChoicesConfig<?> choice : choices) {
            if (choice.getValue().equals(this.topType.toString())) {
                return choice.getName();
            }
        }

        return null;
    }
}
