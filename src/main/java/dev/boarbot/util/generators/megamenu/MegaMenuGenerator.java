package dev.boarbot.util.generators.megamenu;

import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.bot.config.items.ItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MegaMenuGenerator extends ImageGenerator {
    protected final NumberConfig nums = this.config.getNumberConfig();
    protected final PathConfig pathConfig = this.config.getPathConfig();
    protected final StringConfig strConfig = this.config.getStringConfig();
    protected final ItemConfig itemConfig = this.config.getItemConfig();
    protected final Map<String, String> colorConfig = this.config.getColorConfig();

    protected static final int[] IMAGE_SIZE = {1920, 1403};
    protected static final int[] USER_AVATAR_POS = {882, 25};
    protected static final int USER_AVATAR_WIDTH = 156;
    protected static final int[] USER_TAG_POS = {959, 241};
    protected static final int[] DATE_LABEL_POS = {1479, 81};
    protected static final int[] DATE_POS = {1479, 135};
    protected static final int[] NO_BADGE_POS = {442, 108};
    protected static final int BADGE_START_X = 409;
    protected static final int BADGE_SPACING = 68;
    protected static final int BADGE_Y = 56;
    protected static final int[] BADGE_SIZE = {65, 65};

    protected int page;
    protected BoarUser boarUser;

    protected List<String> badgeIDs;
    protected String firstJoinedDate;

    public MegaMenuGenerator(
        int page, BoarUser boarUser, List<String> badgeIDs, String firstJoinedDate
    ) {
        this.page = page;
        this.boarUser = boarUser;
        this.badgeIDs = badgeIDs;
        this.firstJoinedDate = firstJoinedDate;
    }

    protected void drawTopInfo() throws IOException, URISyntaxException {
        String userAvatar = this.boarUser.getUser().getAvatarUrl();

        Map<Class<? extends MegaMenuGenerator>, String> classViewMap = new HashMap<>();
        classViewMap.put(ProfileImageGenerator.class, MegaMenuView.PROFILE.toString());
        classViewMap.put(CollectionImageGenerator.class, MegaMenuView.COLLECTION.toString());
        classViewMap.put(CompendiumImageGenerator.class, MegaMenuView.COMPENDIUM.toString());
        classViewMap.put(EditionsImageGenerator.class, MegaMenuView.EDITIONS.toString());
        classViewMap.put(StatsImageGenerator.class, MegaMenuView.STATS.toString());
        classViewMap.put(PowerupsImageGenerator.class, MegaMenuView.POWERUPS.toString());
        classViewMap.put(QuestsImageGenerator.class, MegaMenuView.QUESTS.toString());
        classViewMap.put(BadgesImageGenerator.class, MegaMenuView.BADGES.toString());

        String view = classViewMap.get(this.getClass());
        String viewString = Character.toUpperCase(view.charAt(0)) + view.substring(1);

        String userString = this.boarUser.getUser().getGlobalName() + "'s " + viewString;

        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawCircleImage(g2d, userAvatar, USER_AVATAR_POS, USER_AVATAR_WIDTH);

        TextDrawer textDrawer = new TextDrawer(
            g2d, userString, USER_TAG_POS, Align.CENTER, colorConfig.get("font"), this.nums.getFontMedium()
        );
        textDrawer.drawText();

        textDrawer.setText(this.strConfig.getMegaMenuDateLabel());
        textDrawer.setPos(DATE_LABEL_POS);
        textDrawer.drawText();

        textDrawer.setText(this.firstJoinedDate);
        textDrawer.setColorVal(this.colorConfig.get("silver"));
        textDrawer.setPos(DATE_POS);
        textDrawer.drawText();

        if (this.badgeIDs.isEmpty()) {
            textDrawer.setText(this.strConfig.getMegaMenuNoBadges());
            textDrawer.setColorVal(this.colorConfig.get("font"));
            textDrawer.setPos(NO_BADGE_POS);
            textDrawer.drawText();
        }

        if (!this.badgeIDs.isEmpty()) {
            int curBadgeStartX = BADGE_START_X - (BADGE_SPACING / 2 * (this.badgeIDs.size() - 1));

            g2d.setPaint(Color.decode(this.colorConfig.get("mid")));
            g2d.fill(new RoundRectangle2D.Double(
                curBadgeStartX - this.nums.getBorder(),
                BADGE_Y - this.nums.getBorder(),
                this.nums.getBorder() * 2 + (this.badgeIDs.size() - 1) * BADGE_SPACING + BADGE_SIZE[0],
                this.nums.getBorder() * 2 + BADGE_SIZE[1],
                this.nums.getBorder() * 2,
                this.nums.getBorder() * 2
            ));

            for (int i=0; i<this.badgeIDs.size(); i++) {
                String badgeID = this.badgeIDs.get(i);
                String badgePath = this.pathConfig.getBadges() + this.itemConfig.getBadges().get(badgeID).getFile();
                int[] badgePos = {curBadgeStartX + i * BADGE_SPACING, BADGE_Y};

                GraphicsUtil.drawImage(g2d, badgePath, badgePos, BADGE_SIZE);
            }
        }
    }
}
