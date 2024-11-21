package dev.boarbot.util.generators.megamenu;

import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MegaMenuGenerator extends ImageGenerator {
    protected static final int[] IMAGE_SIZE = {1920, 1403};
    protected static final int[] USER_AVATAR_POS = {882, 25};
    protected static final int USER_AVATAR_WIDTH = 156;
    private static final int USER_TAG_WIDTH = IMAGE_SIZE[0] - NUMS.getBorder()*4;
    protected static final int[] USER_TAG_POS = {959, 218};
    protected static final int[] DATE_LABEL_POS = {1479, 81};
    protected static final int[] DATE_POS = {1479, 135};
    protected static final int[] NO_BADGE_POS = {442, 108};
    protected static final int BADGE_START_X = 409;
    protected static final int BADGE_SPACING = 68;
    protected static final int BADGE_Y = 56;
    protected static final int[] BADGE_SIZE = {65, 65};

    protected int page;
    protected BoarUser boarUser;

    protected List<BadgeData> badges;
    protected String firstJoinedDate;

    public MegaMenuGenerator(
        int page, BoarUser boarUser, List<BadgeData> badges, String firstJoinedDate
    ) {
        this.page = page;
        this.boarUser = boarUser;
        this.badges = badges;
        this.firstJoinedDate = firstJoinedDate;
    }

    protected void drawTopInfo() throws IOException, URISyntaxException {
        String userAvatar = this.boarUser.getUser().getEffectiveAvatarUrl();

        Map<Class<? extends MegaMenuGenerator>, String> classViewMap = new HashMap<>();
        classViewMap.put(ProfileImageGenerator.class, MegaMenuView.PROFILE.toString());
        classViewMap.put(CollectionImageGenerator.class, MegaMenuView.COLLECTION.toString());
        classViewMap.put(CompendiumImageGenerator.class, MegaMenuView.COMPENDIUM.toString());
        classViewMap.put(EditionsImageGenerator.class, MegaMenuView.EDITIONS.toString());
        classViewMap.put(StatsImageGenerator.class, MegaMenuView.STATS.toString());
        classViewMap.put(PowerupsImageGenerator.class, MegaMenuView.POWERUPS.toString());
        classViewMap.put(QuestsImageGenerator.class, MegaMenuView.QUESTS.toString());
        classViewMap.put(BadgesImageGenerator.class, MegaMenuView.BADGES.toString());
        classViewMap.put(AdventImageGenerator.class, MegaMenuView.ADVENT.toString());

        String view = classViewMap.get(this.getClass());
        String viewString = Character.toUpperCase(view.charAt(0)) + view.substring(1);

        if (view.equals(MegaMenuView.ADVENT.toString())) {
            viewString = STRS.getAdventViewString();
        }

        String userString = this.boarUser.getUser().getEffectiveName() + "'s " + viewString;

        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawCircleImage(g2d, userAvatar, USER_AVATAR_POS, USER_AVATAR_WIDTH);

        TextDrawer textDrawer = new TextDrawer(
            g2d, userString, USER_TAG_POS, Align.CENTER, COLORS.get("font"), NUMS.getFontMedium(), USER_TAG_WIDTH
        );
        textDrawer.drawText();
        textDrawer.setWidth(-1);

        textDrawer.setText(STRS.getMegaMenuDateLabel());
        textDrawer.setPos(DATE_LABEL_POS);
        textDrawer.drawText();

        textDrawer.setText(this.firstJoinedDate);
        textDrawer.setColorVal(COLORS.get("silver"));
        textDrawer.setPos(DATE_POS);
        textDrawer.drawText();

        if (this.badges.isEmpty()) {
            textDrawer.setText(STRS.getMegaMenuNoBadges());
            textDrawer.setColorVal(COLORS.get("font"));
            textDrawer.setPos(NO_BADGE_POS);
            textDrawer.drawText();
        }

        if (!this.badges.isEmpty()) {
            int curBadgeStartX = BADGE_START_X - (BADGE_SPACING / 2 * (this.badges.size() - 1));

            g2d.setPaint(Color.decode(COLORS.get("mid")));
            g2d.fill(new RoundRectangle2D.Double(
                curBadgeStartX - NUMS.getBorder(),
                BADGE_Y - NUMS.getBorder(),
                NUMS.getBorder() * 2 + (this.badges.size() - 1) * BADGE_SPACING + BADGE_SIZE[0],
                NUMS.getBorder() * 2 + BADGE_SIZE[1],
                NUMS.getBorder() * 2,
                NUMS.getBorder() * 2
            ));

            for (int i=0; i<this.badges.size(); i++) {
                String badgeID = this.badges.get(i).badgeID();
                int badgeTier = this.badges.get(i).badgeTier();
                String badgePath = ResourceUtil.badgeAssetsPath + BADGES.get(badgeID).getFiles()[badgeTier];
                int[] badgePos = {curBadgeStartX + i * BADGE_SPACING, BADGE_Y};

                GraphicsUtil.drawImage(g2d, badgePath, badgePos, BADGE_SIZE);
            }
        }
    }
}
