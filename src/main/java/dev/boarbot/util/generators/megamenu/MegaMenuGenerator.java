package dev.boarbot.util.generators.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.bot.config.items.ItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MegaMenuGenerator {
    protected final BotConfig config = BoarBotApp.getBot().getConfig();
    protected final NumberConfig nums = this.config.getNumberConfig();
    protected final PathConfig pathConfig = this.config.getPathConfig();
    protected final StringConfig strConfig = this.config.getStringConfig();
    protected final ItemConfig itemConfig = this.config.getItemConfig();
    protected final Map<String, String> colorConfig = this.config.getColorConfig();

    protected int page;
    protected BoarUser boarUser;

    protected List<String> badgeIDs;
    protected String firstJoinedDate;

    protected BufferedImage generatedImage;

    public MegaMenuGenerator(int page, BoarUser boarUser, List<String> badgeIDs, String firstJoinedDate) {
        this.page = page;
        this.boarUser = boarUser;
        this.badgeIDs = badgeIDs;
        this.firstJoinedDate = firstJoinedDate;
    }

    public abstract FileUpload generate() throws IOException, URISyntaxException;

    protected void drawTopInfo() throws IOException, URISyntaxException {
        int[] avatarPos = this.nums.getCollUserAvatarPos();
        int avatarWidth = this.nums.getCollUserAvatarWidth();
        int[] usernamePos = this.nums.getCollUserTagPos();
        int[] datePos = this.nums.getCollDatePos();

        String userAvatar = this.boarUser.getUser().getAvatarUrl();

        Map<Class<? extends MegaMenuGenerator>, String> classViewMap = new HashMap<>();
        classViewMap.put(CollectionImageGenerator.class, MegaMenuView.COLLECTION.toString());

        String view = classViewMap.get(this.getClass());
        String viewString = Character.toUpperCase(view.charAt(0)) + view.substring(1);

        String userString = this.boarUser.getUser().getGlobalName() + "'s " + viewString;

        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawCircleImage(g2d, userAvatar, avatarPos, avatarWidth);

        TextDrawer textDrawer = new TextDrawer(
                g2d, userString, usernamePos, Align.CENTER, colorConfig.get("font"), this.nums.getFontMedium()
        );
        textDrawer.drawText();

        textDrawer.setText(this.strConfig.getCollDateLabel());
        textDrawer.setPos(this.nums.getCollDateLabelPos());
        textDrawer.drawText();

        textDrawer.setText(this.firstJoinedDate);
        textDrawer.setPos(datePos);
        textDrawer.drawText();

        if (this.badgeIDs.isEmpty()) {
            textDrawer.setText(this.strConfig.getCollNoBadges());
            textDrawer.setPos(this.nums.getCollNoBadgePos());
            textDrawer.drawText();
        }

        if (!this.badgeIDs.isEmpty()) {
            int curBadgeStartX = this.nums.getCollBadgeStart() - (
                this.nums.getCollBadgeSpacing() / 2 * (this.badgeIDs.size() - 1)
            );

            g2d.setPaint(Color.decode(this.colorConfig.get("mid")));
            g2d.fill(new RoundRectangle2D.Double(
                curBadgeStartX - this.nums.getBorder(),
                this.nums.getCollBadgeY() - this.nums.getBorder(),
                this.nums.getBorder() * 2 + (this.badgeIDs.size() - 1) * this.nums.getCollBadgeSpacing() +
                    this.nums.getCollBadgeSize()[0],
                this.nums.getBorder() * 2 + this.nums.getCollBadgeSize()[1],
                this.nums.getBorder() * 2,
                this.nums.getBorder() * 2
            ));

            for (int i=0; i<this.badgeIDs.size(); i++) {
                String badgeID = this.badgeIDs.get(i);
                String badgePath = this.pathConfig.getBadges() + this.itemConfig.getBadges().get(badgeID).getFile();
                int[] badgePos = new int[] {
                    curBadgeStartX + i * this.nums.getCollBadgeSpacing(), this.nums.getCollBadgeY()
                };

                GraphicsUtil.drawImage(g2d, badgePath, badgePos, this.nums.getCollBadgeSize());
            }
        }
    }

    protected FileUpload getFileUpload() throws IOException {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        ImageIO.write(this.generatedImage, "png", byteArrayOS);
        byte[] generatedImageBytes = byteArrayOS.toByteArray();

        return FileUpload.fromData(generatedImageBytes, "unknown.png");
    }
}
