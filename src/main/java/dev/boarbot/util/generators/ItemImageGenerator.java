package dev.boarbot.util.generators;

import dev.boarbot.bot.config.*;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.ImageUtil;
import dev.boarbot.util.graphics.TextDrawer;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ItemImageGenerator {
    private final BotConfig config;

    private final User user;
    private final String title;

    @Getter @Setter private String itemName;
    @Getter @Setter private String filePath;
    @Getter @Setter private String colorKey;
    @Getter @Setter private User giftingUser;
    @Getter @Setter private long score;

    @Getter private BufferedImage generatedImage;

    public ItemImageGenerator(BotConfig config, User user, String title, String itemID) {
        this(config, user, title, itemID, false);
    }

    public ItemImageGenerator(BotConfig config, User user, String title, String itemID, boolean isBadge) {
        this(config, user, title, itemID, isBadge, null);
    }

    public ItemImageGenerator(
        BotConfig config, User user, String title, String itemID, boolean isBadge, User giftingUser
    ) {
        this(config, user, title, itemID, isBadge, giftingUser, -1);
    }

    public ItemImageGenerator(
        BotConfig config, User user, String title, String itemID, boolean isBadge, User giftingUser, long score
    ) {
        this.config = config;
        this.user = user;
        this.title = title;

        if (isBadge) {
            IndivItemConfig badgeInfo = this.config.getItemConfig().getBadges().get(itemID);
            this.itemName = badgeInfo.name;
            this.filePath = this.config.getPathConfig().getBadges() + badgeInfo.file;
            this.colorKey = "badge";
        } else {
            IndivItemConfig boarInfo = this.config.getItemConfig().getBoars().get(itemID);
            this.itemName = boarInfo.name;
            this.filePath = this.config.getPathConfig().getBoars() + boarInfo.file;
            this.colorKey = "rarity" + (BoarUtil.findRarityIndex(itemID, config) + 1);
        }

        this.giftingUser = giftingUser;
        this.score = score;
    }

    public ItemImageGenerator(
        BotConfig config, User user, String title, String itemName, String filePath, String colorKey
    ) {
        this(config, user, title, itemName, filePath, colorKey, null);
    }

    public ItemImageGenerator(
        BotConfig config, User user, String title, String itemName, String filePath, String colorKey, User giftingUser
    ) {
        this(config, user, title, itemName, filePath, colorKey, giftingUser, -1);
    }

    public ItemImageGenerator(
        BotConfig config,
        User user,
        String title,
        String itemName,
        String filePath,
        String colorKey,
        User giftingUser,
        long score
    ) {
        this.config = config;
        this.user = user;
        this.title = title;

        this.itemName = itemName;
        this.filePath = filePath;
        this.colorKey = colorKey;

        this.giftingUser = giftingUser;
        this.score = score;
    }

    public FileUpload generate() throws IOException {
        String imageExtension = this.filePath.split("[.]")[1];
        boolean isAnimated = imageExtension.equals("gif");

        String userAvatar = this.user.getEffectiveAvatarUrl();
        String username = this.user.getName().substring(
            0, Math.min(this.user.getName().length(), this.config.getNumberConfig().getMaxUsernameLength())
        );

        String giftingUserAvatar = null;
        String giftingUsername = null;

        if (this.giftingUser != null) {
            giftingUserAvatar = this.giftingUser.getEffectiveAvatarUrl();
            giftingUsername = this.giftingUser.getName().substring(
                0, Math.min(this.giftingUser.getName().length(), this.config.getNumberConfig().getMaxUsernameLength())
            );
        }

        if (isAnimated) {
            this.generateStatic(false);
            this.generateAnimated();
            this.addAnimatedUser();
        } else {
            this.generateStatic(true);
            this.addStaticUser();
        }

        return FileUpload.fromData(ImageUtil.toFile(this.generatedImage));
    }

    private void generateAnimated() {

    }

    private void addAnimatedUser() {

    }

    private void generateStatic(boolean makeWithItem) throws IOException {
        StringConfig strConfig = this.config.getStringConfig();
        NumberConfig nums = this.config.getNumberConfig();
        PathConfig pathConfig = this.config.getPathConfig();
        Map<String, String> colorConfig = this.config.getColorConfig();

        String itemAssetsFolder = pathConfig.getItemAssets();
        String underlayPath = itemAssetsFolder + pathConfig.getItemUnderlay();
        String backplatePath = itemAssetsFolder + pathConfig.getItemBackplate();

        int[] origin = nums.getOriginPos();
        int[] imageSize = nums.getItemImageSize();

        int[] mainPos = nums.getItemPos();
        int[] mainSize = nums.getBigBoarSize();

        this.generatedImage = new BufferedImage(imageSize[0], imageSize[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.generatedImage.createGraphics();

        GraphicsUtil.drawRect(g2d, origin, imageSize, colorConfig.get(colorKey));
        g2d.setComposite(AlphaComposite.DstIn);
        GraphicsUtil.drawImage(g2d, underlayPath, origin, imageSize);
        g2d.setComposite(AlphaComposite.SrcOver);

        GraphicsUtil.drawImage(g2d, backplatePath, origin, imageSize);
        if (makeWithItem) {
            GraphicsUtil.drawImage(g2d, this.filePath, mainPos, mainSize);
        }

        TextDrawer textDrawer = new TextDrawer(
            g2d,
            this.title,
            nums.getItemTitlePos(),
            Align.CENTER,
            colorConfig.get("font"),
            nums.getFontMedium(),
            config
        );

        textDrawer.drawText();

        textDrawer.setText(this.itemName);
        textDrawer.setPos(nums.getItemNamePos());
        textDrawer.setColorStr(colorConfig.get(this.colorKey));

        textDrawer.drawText();
    }

    private void addStaticUser() {

    }
}
