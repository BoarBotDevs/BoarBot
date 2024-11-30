package dev.boarbot.util.generators.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.commands.boar.megamenu.AdventSubcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.AdventData;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.interactives.boar.megamenu.MegaMenuInteractive;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.resource.ResourceUtil;
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class AdventImageGenerator extends MegaMenuGenerator {
    private static final int REWARDS_START_X = 25;
    private static final int REWARDS_START_Y = 268;
    private static final int NUM_COLS = 5;
    private static final int[] BONUS_POS = {1350, 827};
    private static final int[] TEXT_START_POS = {1527, 406};
    private static final int VALUE_Y_OFFSET = 78;
    private static final int LABEL_Y_SPACING = 198;
    private static final int BONUS_TEXT_Y = 1259;
    private static final int AMOUNT_X_OFFSET = 9;
    private static final int AMOUNT_Y_OFFSET = 38;
    private static final int AMOUNT_BOX_WIDTH_OFFSET = 20;
    private static final int AMOUNT_BOX_HEIGHT = 50;

    private final AdventData adventData;

    public AdventImageGenerator(
        int page, BoarUser boarUser, List<BadgeData> badges, String firstJoinedDate, AdventData adventData
    ) {
        super(page, boarUser, badges, firstJoinedDate);
        this.adventData = adventData;
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        int border = NUMS.getBorder();
        int[] rewardSize = NUMS.getSmallBoarSize();
        int[] bonusSize = NUMS.getMediumBoarSize();

        int mediumFont = NUMS.getFontMedium();

        int[] claimedPos = {TEXT_START_POS[0], TEXT_START_POS[1] + VALUE_Y_OFFSET};
        int numClaimed = 0;

        int[] nextLabelPos = {TEXT_START_POS[0], TEXT_START_POS[1] + LABEL_Y_SPACING};
        int[] nextPos = {TEXT_START_POS[0], nextLabelPos[1] + VALUE_Y_OFFSET};
        String nextStr = TimeUtil.getDayOfMonth() < 25
            ? TimeUtil.getTimeDistance(TimeUtil.getNextDailyResetMilli(), false)
            : STRS.getUnavailable();
        nextStr = Character.toUpperCase(nextStr.charAt(0)) + nextStr.substring(1);

        int[] bonusLabelPos = {TEXT_START_POS[0], nextLabelPos[1] + LABEL_Y_SPACING};
        int[] bonusPos = {TEXT_START_POS[0], BONUS_TEXT_Y};
        String bonusStr = STRS.getNoBonusLabel();

        if (this.adventData.adventBits() == MegaMenuInteractive.FULL_ADVENT_BITS) {
            bonusStr = STRS.getUnclaimedBonusLabel();
        } else if (this.adventData.adventBits() > MegaMenuInteractive.FULL_ADVENT_BITS) {
            bonusStr = STRS.getClaimedBonusLabel();
        }

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.adventUnderlayPath, ORIGIN, IMAGE_SIZE);

        for (int i=0; i<MegaMenuInteractive.LAST_ADVENT_DAY; i++) {
            int[] rewardPos = {
                REWARDS_START_X + (i % NUM_COLS) * (rewardSize[0] + border),
                REWARDS_START_Y + (i / NUM_COLS) * (rewardSize[1] + border)
            };

            boolean claimed = (this.adventData.adventBits() >> i) % 2 == 1 ||
                this.adventData.adventBits() > MegaMenuInteractive.FULL_ADVENT_BITS;
            numClaimed += claimed ? 1 : 0;
            String giftToDraw = this.getGiftToDraw(i, claimed);

            GraphicsUtil.drawImage(g2d, giftToDraw, rewardPos, rewardSize);

            String dateColor = COLORS.get("font");

            if (i+1 == TimeUtil.getDayOfMonth()) {
                BufferedImage rarityBorderImage = BoarBotApp.getBot().getImageCacheMap().get("borderSmall" + "gold");
                g2d.drawImage(rarityBorderImage, rewardPos[0], rewardPos[1], null);
                dateColor = COLORS.get("gold");
            }

            int[] datePos = new int[] {rewardPos[0] + AMOUNT_X_OFFSET, rewardPos[1] + AMOUNT_Y_OFFSET};
            String dateStr = Integer.toString(i+1);
            TextDrawer textDrawer = new TextDrawer(
                g2d, dateStr, datePos, Align.LEFT, dateColor, NUMS.getFontSmallest()
            );
            FontMetrics fm = g2d.getFontMetrics();
            int[] rectangleSize = new int[] {
                fm.stringWidth(dateStr) + AMOUNT_BOX_WIDTH_OFFSET + border,
                AMOUNT_BOX_HEIGHT + border
            };

            g2d.setPaint(Color.decode(COLORS.get("dark")));
            g2d.fill(new RoundRectangle2D.Double(
                rewardPos[0]-border,
                rewardPos[1]-border,
                rectangleSize[0],
                rectangleSize[1],
                NUMS.getBorder() * 2,
                NUMS.getBorder() * 2
            ));

            textDrawer.drawText();
        }

        this.textDrawer = new TextDrawer(
            g2d, STRS.getCompAmountLabel(), TEXT_START_POS, Align.CENTER, COLORS.get("font"), mediumFont
        );

        String rewardStr = numClaimed == MegaMenuInteractive.LAST_ADVENT_DAY
            ? "<>gold<>%,d<>silver<>/<>gold<>%,d".formatted(numClaimed, MegaMenuInteractive.LAST_ADVENT_DAY)
            : "<>error<>%,d<>silver<>/<>green<>%,d".formatted(numClaimed, MegaMenuInteractive.LAST_ADVENT_DAY);

        TextUtil.drawLabel(this.textDrawer, STRS.getAdventClaimedLabel(), TEXT_START_POS);
        TextUtil.drawValue(this.textDrawer, rewardStr.formatted(0, MegaMenuInteractive.LAST_ADVENT_DAY), claimedPos);

        TextUtil.drawLabel(this.textDrawer, STRS.getAdventNextLabel(), nextLabelPos);
        TextUtil.drawValue(this.textDrawer, nextStr, nextPos);

        TextUtil.drawLabel(this.textDrawer, STRS.getAdventBonusLabel(), bonusLabelPos);
        TextUtil.drawValue(this.textDrawer, bonusStr, bonusPos);

        String bonusGiftImage = ResourceUtil.unavailableAdventPath;

        if (this.adventData.adventBits() > MegaMenuInteractive.FULL_ADVENT_BITS) {
            bonusGiftImage = ResourceUtil.eventAdventPath;
        } else if (this.adventData.adventBits() == MegaMenuInteractive.FULL_ADVENT_BITS) {
            bonusGiftImage = ResourceUtil.currentAdventPath;
        }

        GraphicsUtil.drawImage(g2d, bonusGiftImage, BONUS_POS, bonusSize);

        this.drawTopInfo();
        return this;
    }

    private String getGiftToDraw(int i, boolean claimed) {
        if (i+1 == TimeUtil.getDayOfMonth() && !claimed) {
            return ResourceUtil.currentAdventPath;
        }

        if (i+1 <= TimeUtil.getDayOfMonth()) {
            if (!claimed && this.adventData.adventBits() < MegaMenuInteractive.FULL_ADVENT_BITS) {
                return ResourceUtil.missedAdventPath;
            }

            AdventSubcommand.RewardType rewardType = AdventSubcommand.getBaseRewardType(i+1);

            switch (rewardType) {
                case BUCKS -> {
                    return ResourceUtil.bucksAdventPath;
                }

                case BLESSINGS -> {
                    return ResourceUtil.blessingAdventPath;
                }

                case CELESTICON -> {
                    return ResourceUtil.celesticonAdventPath;
                }

                case FESTIVE -> {
                    return ResourceUtil.festiveAdventPath;
                }

                case POWERUP -> {
                    return ResourceUtil.powerupAssetsPath + POWS.get("gift").getFile();
                }
            }
        }

        return ResourceUtil.unavailableAdventPath;
    }
}
