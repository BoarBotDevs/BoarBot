package dev.boarbot.util.generators.megamenu;

import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.entities.boaruser.data.PowerupsData;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class PowerupsImageGenerator extends MegaMenuGenerator {
    private static final int LEFT_START_X = 480;
    private static final int LEFT_START_Y = 484;
    private static final int RIGHT_TEXT_X = 1393;
    private static final int RIGHT_LABEL_Y = 447;
    private static final int[] CELL_POS = {1201, 526};
    private static final int[] CELL_SIZE = {382, 551};
    private static final int RIGHT_VALUE_Y = 1197;
    private static final int RIGHT_DRIFT_Y = 1268;
    private static final int VALUE_Y_OFFSET = 78;
    private static final int LABEL_Y_SPACING = 198;

    private final PowerupsData powData;

    public PowerupsImageGenerator(
        int page,
        BoarUser boarUser,
        List<BadgeData> badges,
        String firstJoinedDate,
        PowerupsData powData
    ) {
        super(page, boarUser, badges, firstJoinedDate);
        this.powData = powData;
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        String cellPath = ResourceUtil.powCellNonePath;

        int numTransmute = this.powData.powAmts().get("transmute") == null
            ? 0
            : this.powData.powAmts().get("transmute");
        String cellValueStr = STRS.getPowCellAmtLabel();
        String transmuteRarityKey = null;

        if (numTransmute > RARITIES.get("divine").getChargesNeeded()) {
            cellValueStr = STRS.getPowCellErrorLabel();
        } else if (numTransmute >= RARITIES.get("divine").getChargesNeeded()) {
            cellPath = ResourceUtil.powCellDivinePath;
            transmuteRarityKey = "divine";
        } else if (numTransmute >= RARITIES.get("mythic").getChargesNeeded()) {
            cellPath = ResourceUtil.powCellMythicPath;
            transmuteRarityKey = "mythic";
        } else if (numTransmute >= RARITIES.get("legendary").getChargesNeeded()) {
            cellPath = ResourceUtil.powCellLegendaryPath;
            transmuteRarityKey = "legendary";
        } else if (numTransmute >= RARITIES.get("epic").getChargesNeeded()) {
            cellPath = ResourceUtil.powCellEpicPath;
            transmuteRarityKey = "epic";
        } else if (numTransmute >= RARITIES.get("rare").getChargesNeeded()) {
            cellPath = ResourceUtil.powCellRarePath;
            transmuteRarityKey = "rare";
        } else if (numTransmute >= RARITIES.get("uncommon").getChargesNeeded()) {
            cellPath = ResourceUtil.powCellUncommonPath;
            transmuteRarityKey = "uncommon";
        } else if (numTransmute >= RARITIES.get("common").getChargesNeeded()) {
            cellPath = ResourceUtil.powCellCommonPath;
            transmuteRarityKey = "common";
        } else {
            cellValueStr = STRS.getPowCellEmptyLabel();
        }

        if (transmuteRarityKey != null) {
            cellValueStr = cellValueStr.formatted(
                transmuteRarityKey,
                RARITIES.get(transmuteRarityKey).getName(),
                numTransmute,
                RARITIES.get("divine").getChargesNeeded()
            );
        }

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        if (numTransmute <= RARITIES.get("divine").getChargesNeeded()) {
            GraphicsUtil.drawImage(g2d, ResourceUtil.megaMenuBasePath, ORIGIN, IMAGE_SIZE);
        } else {
            GraphicsUtil.drawImage(g2d, ResourceUtil.powAnomUnderlayPath, ORIGIN, IMAGE_SIZE);
        }

        if (numTransmute <= RARITIES.get("divine").getChargesNeeded()) {
            GraphicsUtil.drawImage(g2d, cellPath, CELL_POS, CELL_SIZE);
        }

        this.textDrawer = new TextDrawer(g2d, "", ORIGIN, Align.CENTER, COLORS.get("font"), NUMS.getFontMedium());

        int[] blessingsLabelPos = {LEFT_START_X, LEFT_START_Y};
        String blessHex = TextUtil.getBlessHex(this.powData.blessings(), this.powData.miraclesActive());
        String blessingsStr = this.powData.miraclesActive()
            ? "%s %,d".formatted(STRS.getBlessingsSymbol(), this.powData.blessings())
            : "%,d".formatted(this.powData.blessings());
        int[] blessingsPos = {LEFT_START_X, blessingsLabelPos[1] + VALUE_Y_OFFSET};

        int[] miraclesLabelPos = {LEFT_START_X, blessingsLabelPos[1] + LABEL_Y_SPACING};
        String miraclesStr = this.powData.powAmts().get("miracle") == null
            ? "0"
            : "%,d".formatted(this.powData.powAmts().get("miracle"));
        int[] miraclesPos = {LEFT_START_X, miraclesLabelPos[1] + VALUE_Y_OFFSET};

        int[] cloneLabelPos = {LEFT_START_X, miraclesLabelPos[1] + LABEL_Y_SPACING};
        String cloneStr = this.powData.powAmts().get("clone") == null
            ? "0"
            : "%,d".formatted(this.powData.powAmts().get("clone"));
        int[] clonePos = {LEFT_START_X, cloneLabelPos[1] + VALUE_Y_OFFSET};

        int[] giftsLabelPos = {LEFT_START_X, cloneLabelPos[1] + LABEL_Y_SPACING};
        String giftsStr = this.powData.powAmts().get("gift") == null
            ? "0"
            : "%,d".formatted(this.powData.powAmts().get("gift"));
        int[] giftsPos = {LEFT_START_X, giftsLabelPos[1] + VALUE_Y_OFFSET};

        int[] cellLabelPos = {RIGHT_TEXT_X, RIGHT_LABEL_Y};
        int[] cellValuePos = {RIGHT_TEXT_X, RIGHT_VALUE_Y};

        TextUtil.drawLabel(this.textDrawer, STRS.getBlessingsPluralName(), blessingsLabelPos);
        TextUtil.drawValue(this.textDrawer, blessingsStr, blessingsPos, false, blessHex);

        TextUtil.drawLabel(this.textDrawer, POWS.get("miracle").getPluralName(), miraclesLabelPos);
        TextUtil.drawValue(this.textDrawer, miraclesStr, miraclesPos);

        TextUtil.drawLabel(this.textDrawer, POWS.get("clone").getPluralName(), cloneLabelPos);
        TextUtil.drawValue(this.textDrawer, cloneStr, clonePos);

        TextUtil.drawLabel(this.textDrawer, POWS.get("gift").getPluralName(), giftsLabelPos);
        TextUtil.drawValue(this.textDrawer, giftsStr, giftsPos);

        TextUtil.drawLabel(this.textDrawer, STRS.getPowCellLabel(), cellLabelPos);
        TextUtil.drawLabel(this.textDrawer, cellValueStr, cellValuePos);

        if (numTransmute > RARITIES.get("divine").getChargesNeeded()) {
            String cellDriftStr = STRS.getPowCellDriftLabel()
                .formatted(numTransmute - RARITIES.get("divine").getChargesNeeded());
            int[] cellDriftPos = {RIGHT_TEXT_X, RIGHT_DRIFT_Y};

            TextUtil.drawLabel(this.textDrawer, cellDriftStr, cellDriftPos);
        }


        this.drawTopInfo();
        return this;
    }
}
