package dev.boarbot.util.generators.megamenu;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.entities.boaruser.data.PowerupsData;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class PowerupsImageGenerator extends MegaMenuGenerator {
    private static final int[] ORIGIN = {0, 0};
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
        Map<String, PowerupItemConfig> pows = this.itemConfig.getPowerups();
        Map<String, RarityConfig> rarityConfig = this.config.getRarityConfigs();
        String underlayPath = this.pathConfig.getMegaMenuAssets() + this.pathConfig.getMegaMenuBase();
        String anomalousUnderlayPath = this.pathConfig.getMegaMenuAssets() + this.pathConfig.getPowAnomUnderlay();
        String cellPath = this.pathConfig.getMegaMenuAssets();

        int numTransmute = this.powData.powAmts().get("transmute") == null
            ? 0
            : this.powData.powAmts().get("transmute");
        String cellValueStr = this.strConfig.getPowCellAmtLabel();
        String transmuteRarityKey = null;

        if (numTransmute == rarityConfig.get("common").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellCommon();
            transmuteRarityKey = "common";
        } else if (numTransmute == rarityConfig.get("uncommon").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellUncommon();
            transmuteRarityKey = "uncommon";
        } else if (numTransmute == rarityConfig.get("rare").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellRare();
            transmuteRarityKey = "rare";
        } else if (numTransmute == rarityConfig.get("epic").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellEpic();
            transmuteRarityKey = "epic";
        } else if (numTransmute == rarityConfig.get("legendary").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellLegendary();
            transmuteRarityKey = "legendary";
        } else if (numTransmute == rarityConfig.get("mythic").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellMythic();
            transmuteRarityKey = "mythic";
        } else if (numTransmute == rarityConfig.get("divine").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellDivine();
            transmuteRarityKey = "divine";
        } else if (numTransmute > rarityConfig.get("divine").getChargesNeeded()) {
            cellPath += this.pathConfig.getPowCellEntropic();
            cellValueStr = this.strConfig.getPowCellErrorLabel();
        } else {
            cellPath += this.pathConfig.getPowCellNone();
            cellValueStr = this.strConfig.getPowCellEmptyLabel();
        }

        if (transmuteRarityKey != null) {
            cellValueStr = cellValueStr.formatted(
                transmuteRarityKey,
                rarityConfig.get(transmuteRarityKey).getName(),
                numTransmute,
                rarityConfig.get("divine").getChargesNeeded()
            );
        }

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        if (numTransmute <= rarityConfig.get("divine").getChargesNeeded()) {
            GraphicsUtil.drawImage(g2d, underlayPath, ORIGIN, IMAGE_SIZE);
        } else {
            GraphicsUtil.drawImage(g2d, anomalousUnderlayPath, ORIGIN, IMAGE_SIZE);
        }

        if (numTransmute <= rarityConfig.get("divine").getChargesNeeded()) {
            GraphicsUtil.drawImage(g2d, cellPath, CELL_POS, CELL_SIZE);
        }

        this.textDrawer = new TextDrawer(
            g2d, "", ORIGIN, Align.CENTER, this.colorConfig.get("font"), this.nums.getFontMedium()
        );

        int[] blessingsLabelPos = {LEFT_START_X, LEFT_START_Y};
        String blessHex = TextUtil.getBlessHex(this.powData.blessings());
        String blessingsStr = this.powData.blessings() > 1000
            ? "%s %,d".formatted(this.strConfig.getBlessingsSymbol(), this.powData.blessings())
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

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getBlessingsPluralName(), blessingsLabelPos);
        TextUtil.drawValue(this.textDrawer, blessingsStr, blessingsPos, false, blessHex);

        TextUtil.drawLabel(this.textDrawer, pows.get("miracle").getPluralName(), miraclesLabelPos);
        TextUtil.drawValue(this.textDrawer, miraclesStr, miraclesPos);

        TextUtil.drawLabel(this.textDrawer, pows.get("clone").getPluralName(), cloneLabelPos);
        TextUtil.drawValue(this.textDrawer, cloneStr, clonePos);

        TextUtil.drawLabel(this.textDrawer, pows.get("gift").getPluralName(), giftsLabelPos);
        TextUtil.drawValue(this.textDrawer, giftsStr, giftsPos);

        TextUtil.drawLabel(this.textDrawer, this.strConfig.getPowCellLabel(), cellLabelPos);
        TextUtil.drawLabel(this.textDrawer, cellValueStr, cellValuePos);

        if (numTransmute > rarityConfig.get("divine").getChargesNeeded()) {
            String cellDriftStr = this.strConfig.getPowCellDriftLabel()
                .formatted(numTransmute - rarityConfig.get("divine").getChargesNeeded());
            int[] cellDriftPos = {RIGHT_TEXT_X, RIGHT_DRIFT_Y};

            TextUtil.drawLabel(this.textDrawer, cellDriftStr, cellDriftPos);
        }


        this.drawTopInfo();
        return this;
    }
}
