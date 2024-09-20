package dev.boarbot.util.generators.megamenu;

import dev.boarbot.bot.config.items.BadgeItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.graphics.TextUtil;
import dev.boarbot.util.resource.ResourceUtil;
import dev.boarbot.util.time.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class BadgesImageGenerator extends MegaMenuGenerator {
    private static final int[] LEFT_POS = {484, 762};
    private static final int VALUE_Y_OFFSET = 78;
    private static final int[] RARITY_POS = {1431, 403};
    private static final int[] NAME_POS = {1431, 469};
    private static final int[] BADGE_POS = {1108, 475};
    private static final int RIGHT_WIDTH = 800;
    private static final int[] DESCRIPTION_POS = {1431, 1169};

    public BadgesImageGenerator(int page, BoarUser boarUser, List<BadgeData> badges, String firstJoinedDate) {
        super(page, boarUser, badges, firstJoinedDate);
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        int mediumFont = NUMS.getFontMedium();
        int smallestFont = NUMS.getFontSmallest();

        BadgeItemConfig badge = BADGES.get(this.badges.get(this.page).badgeID());
        int badgeTier = this.badges.get(this.page).badgeTier();
        long badgeObtained = this.badges.get(this.page).obtainedTimestamp();

        String badgeFile = ResourceUtil.badgeAssetsPath + badge.getFiles()[badgeTier];
        String badgeDescription = badge.getDescriptions()[badgeTier];

        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.badgeUnderlayPath, ORIGIN, IMAGE_SIZE);

        this.textDrawer = new TextDrawer(g2d, "", ORIGIN, Align.CENTER, COLORS.get("font"), mediumFont);

        String obtainedStr = Instant.ofEpochMilli(badgeObtained)
            .atOffset(ZoneOffset.UTC)
            .format(TimeUtil.getDateFormatter());
        int[] obtainedPos = {LEFT_POS[0], LEFT_POS[1] + VALUE_Y_OFFSET};

        TextUtil.drawLabel(this.textDrawer, STRS.getBadgeObtainedLabel(), LEFT_POS);
        TextUtil.drawValue(this.textDrawer, obtainedStr, obtainedPos);

        this.textDrawer.setText("BADGE");
        this.textDrawer.setPos(RARITY_POS);
        this.textDrawer.setFontSize(mediumFont);
        this.textDrawer.setColorVal(COLORS.get("badge"));
        this.textDrawer.setWidth(RIGHT_WIDTH);
        this.textDrawer.drawText();

        this.textDrawer.setText(badge.getNames()[badgeTier]);
        this.textDrawer.setPos(NAME_POS);
        this.textDrawer.setColorVal(COLORS.get("font"));
        this.textDrawer.drawText();

        GraphicsUtil.drawImage(g2d, badgeFile, BADGE_POS, NUMS.getMediumBigBoarSize());

        this.textDrawer.setText(badgeDescription);
        this.textDrawer.setPos(DESCRIPTION_POS);
        this.textDrawer.setFontSize(smallestFont);
        this.textDrawer.setWrap(true);
        this.textDrawer.drawText();

        this.drawTopInfo();
        return this;
    }
}
