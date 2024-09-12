package dev.boarbot.util.generators.megamenu;

import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public class EditionsImageGenerator extends MegaMenuGenerator {
    private static final int START_Y = 720;
    private static final int VALUE_Y_OFFSET = 78;
    private static final int LABEL_Y_SPACING = 198;

    private final String boarID;
    private final BoarInfo boarInfo;

    public EditionsImageGenerator(
        int page,
        BoarUser boarUser,
        List<BadgeData> badges,
        String firstJoinedDate,
        Map.Entry<String, BoarInfo> boarEntry
    ) {
        super(page, boarUser, badges, firstJoinedDate);
        this.boarID = boarEntry.getKey();
        this.boarInfo = boarEntry.getValue();
    }

    @Override
    public ImageGenerator generate() throws IOException, URISyntaxException {
        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        int bigFont = NUMS.getFontBig();

        String underlayPath = PATHS.getMegaMenuAssets() + PATHS.getMegaMenuBase();

        GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN, IMAGE_SIZE);

        TextDrawer textDrawer = new TextDrawer(g2d, "", ORIGIN, Align.CENTER, COLORS.get("font"), bigFont);

        int xPos = this.generatedImage.getWidth() / 2;
        int pageNumEditions = Math.min(this.boarInfo.getEditions().size(), (this.page + 1) * 5) - this.page * 5;
        int curStartY = START_Y - (pageNumEditions - 1) * VALUE_Y_OFFSET;

        for (int i=0; i<pageNumEditions; i++) {
            int curIndex = this.page * 5 + i;

            String editionText = "<>%s<>%s<>font<> #%d".formatted(
                this.boarInfo.getRarityID(), BOARS.get(this.boarID).getName(), this.boarInfo.getEditions().get(curIndex)
            );

            textDrawer.setText(editionText);
            textDrawer.setPos(new int[] {xPos, curStartY + i * LABEL_Y_SPACING});
            textDrawer.setColorVal(COLORS.get("font"));
            textDrawer.drawText();

            textDrawer.setText(
                Instant.ofEpochMilli(this.boarInfo.getEditionTimestamps().get(curIndex))
                    .atOffset(ZoneOffset.UTC)
                    .format(TimeUtil.getTimeFormatter())
            );
            textDrawer.setPos(new int[] {xPos, curStartY + i * LABEL_Y_SPACING + VALUE_Y_OFFSET});
            textDrawer.setColorVal(COLORS.get("silver"));
            textDrawer.drawText();
        }

        this.drawTopInfo();
        return this;
    }
}
