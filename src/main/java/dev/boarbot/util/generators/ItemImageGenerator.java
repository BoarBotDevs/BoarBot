package dev.boarbot.util.generators;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.*;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Map;

public class ItemImageGenerator {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    private final User user;
    private final String title;

    @Getter @Setter private String itemName;
    @Getter @Setter private String filePath;
    @Getter @Setter private String colorKey;
    @Getter @Setter private User giftingUser;
    @Getter @Setter private long bucks;

    private String transparentColor;

    private byte[] generatedImageByteArray;

    public ItemImageGenerator(User user, String title, String itemID) {
        this(user, title, itemID, false);
    }

    public ItemImageGenerator(User user, String title, String itemID, boolean isBadge) {
        this(user, title, itemID, isBadge, null);
    }

    public ItemImageGenerator(User user, String title, String itemID, boolean isBadge, User giftingUser) {
        this(user, title, itemID, isBadge, giftingUser, -1);
    }

    public ItemImageGenerator(User user, String title, String itemID, boolean isBadge, User giftingUser, long bucks) {
        this.user = user;
        this.title = title;

        if (isBadge) {
            IndivItemConfig badgeInfo = this.config.getItemConfig().getBadges().get(itemID);
            this.itemName = badgeInfo.name;
            this.filePath = this.config.getPathConfig().getBadges() + badgeInfo.file;
            this.colorKey = "badge";

            if (badgeInfo.transparentColor != null) {
                this.transparentColor = badgeInfo.transparentColor;
            }
        } else {
            IndivItemConfig boarInfo = this.config.getItemConfig().getBoars().get(itemID);
            this.itemName = boarInfo.name;
            this.filePath = this.config.getPathConfig().getBoars() + boarInfo.file;
            this.colorKey = BoarUtil.findRarityKey(itemID);

            if (boarInfo.transparentColor != null) {
                this.transparentColor = boarInfo.transparentColor;
            }
        }

        this.giftingUser = giftingUser;
        this.bucks = bucks;
    }

    public ItemImageGenerator(User user, String title, String itemName, String filePath, String colorKey) {
        this(user, title, itemName, filePath, colorKey, null);
    }

    public ItemImageGenerator(
        User user, String title, String itemName, String filePath, String colorKey, User giftingUser
    ) {
        this(user, title, itemName, filePath, colorKey, giftingUser, -1);
    }

    public ItemImageGenerator(
        User user, String title, String itemName, String filePath, String colorKey, User giftingUser, long bucks
    ) {
        this.user = user;
        this.title = title;

        this.itemName = itemName;
        this.filePath = filePath;
        this.colorKey = colorKey;

        this.giftingUser = giftingUser;
        this.bucks = bucks;
    }

    public FileUpload generate() throws IOException, URISyntaxException {
        String extension = this.filePath.split("[.]")[1];

        this.generatedImageByteArray = BoarBotApp.getBot().getCacheMap().get("item" + this.itemName + this.colorKey);

        if (extension.equals("gif")) {
            if (this.generatedImageByteArray == null) {
                this.generateStatic(false);
                this.generateAnimated();

                BoarBotApp.getBot().getCacheMap().put(
                    "item" + this.itemName + this.colorKey, this.generatedImageByteArray
                );
            }

            this.addAnimatedUser();
        } else {
            if (this.generatedImageByteArray == null) {
                this.generateStatic(true);

                BoarBotApp.getBot().getCacheMap().put(
                    "item" + this.title.toLowerCase().replaceAll("[^a-z]+", "") + this.itemName + this.colorKey,
                    this.generatedImageByteArray
                );
            }

            this.addStaticUser();
        }

        return FileUpload.fromData(this.generatedImageByteArray, "unknown." + extension);
    }

    private void generateAnimated() throws IOException {
        NumberConfig nums = this.config.getNumberConfig();

        GifDecoder decoder = new GifDecoder();
        decoder.read(new BufferedInputStream(new FileInputStream(this.filePath)));
        int numFrames = decoder.getFrameCount();

        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

        this.initGifEncoder(encoder, byteArrayOS);

        ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(this.generatedImageByteArray);
        BufferedImage generatedImage = ImageIO.read(byteArrayIS);
        Graphics2D g2d = generatedImage.createGraphics();

        int[] mainPos = nums.getItemPos();
        int[] mainSize = nums.getBigBoarSize();

        for (int i=0; i<numFrames; i++) {
            BufferedImage frame = decoder.getFrame(i);
            int delay = decoder.getDelay(i);

            g2d.drawImage(frame, mainPos[0], mainPos[1], mainSize[0], mainSize[1], null);

            encoder.setDelay(delay);
            encoder.addFrame(generatedImage);
        }

        encoder.finish();

        this.generatedImageByteArray = byteArrayOS.toByteArray();
    }

    private void addAnimatedUser() throws IOException, URISyntaxException {
        NumberConfig nums = this.config.getNumberConfig();

        GifDecoder decoder = new GifDecoder();
        decoder.read(new BufferedInputStream(new ByteArrayInputStream(this.generatedImageByteArray)));
        int numFrames = decoder.getFrameCount();

        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

        this.initGifEncoder(encoder, byteArrayOS);

        BufferedImage generatedUserImageData = this.generateUserImageData();

        for (int i=0; i<numFrames; i++) {
            BufferedImage frame = decoder.getFrame(i);
            Graphics2D g2d = frame.createGraphics();
            int delay = decoder.getDelay(i);

            g2d.drawImage(generatedUserImageData, nums.getItemBoxX(), nums.getItemBoxOneY(), null);

            encoder.setDelay(delay);
            encoder.addFrame(frame);
        }

        encoder.finish();

        this.generatedImageByteArray = byteArrayOS.toByteArray();
    }

    private void initGifEncoder(AnimatedGifEncoder encoder, ByteArrayOutputStream byteArrayOS) {
        encoder.setRepeat(0);
        encoder.setDispose(2);
        encoder.setQuality(20);
        encoder.setBackground(Color.decode(this.transparentColor));
        encoder.setTransparent(Color.decode(this.transparentColor));
        encoder.start(new BufferedOutputStream(byteArrayOS));
    }

    private void generateStatic(boolean makeWithItem) throws IOException, URISyntaxException {
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

        BufferedImage generatedImage = new BufferedImage(imageSize[0], imageSize[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

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
            nums.getFontMedium()
        );

        textDrawer.drawText();

        textDrawer.setText(this.itemName);
        textDrawer.setPos(nums.getItemNamePos());
        textDrawer.setColorVal(colorConfig.get(this.colorKey));
        textDrawer.setWidth(mainSize[0]);

        textDrawer.drawText();

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        ImageIO.write(generatedImage, "png", byteArrayOS);
        this.generatedImageByteArray = byteArrayOS.toByteArray();
    }

    private void addStaticUser() throws IOException, URISyntaxException {
        NumberConfig nums = this.config.getNumberConfig();

        ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(this.generatedImageByteArray);
        BufferedImage generatedImage = ImageIO.read(byteArrayIS);
        Graphics2D g2d = generatedImage.createGraphics();

        g2d.drawImage(this.generateUserImageData(), nums.getItemBoxX(), nums.getItemBoxOneY(), null);

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        ImageIO.write(generatedImage, "png", byteArrayOS);
        this.generatedImageByteArray = byteArrayOS.toByteArray();
    }

    private BufferedImage generateUserImageData() throws IOException, URISyntaxException {
        NumberConfig nums = this.config.getNumberConfig();
        Map<String, String> colorConfig = this.config.getColorConfig();

        int[] origin = nums.getOriginPos();
        int[] imageSize = nums.getItemImageSize();

        int userBoxY = nums.getItemBoxOneY();

        String userAvatar = this.user.getEffectiveAvatarUrl();
        String username = this.user.getName().substring(
            0, Math.min(this.user.getName().length(), this.config.getNumberConfig().getMaxUsernameLength())
        );

        BufferedImage userDataImage = new BufferedImage(imageSize[0], imageSize[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = userDataImage.createGraphics();

        TextDrawer textDrawer = new TextDrawer(
            g2d, "", origin, Align.LEFT, colorConfig.get("font"), nums.getFontSmallMedium()
        );
        FontMetrics fm = g2d.getFontMetrics();

        if (this.giftingUser != null) {
            userBoxY = nums.getItemBoxTwoY();

            String giftingUserAvatar = this.giftingUser.getEffectiveAvatarUrl();
            String giftingUsername = this.giftingUser.getName().substring(
                0, Math.min(this.giftingUser.getName().length(), this.config.getNumberConfig().getMaxUsernameLength())
            );

            g2d.setPaint(Color.decode(colorConfig.get("mid")));
            g2d.fillRect(nums.getItemBoxX(), nums.getItemBoxOneY(), nums.getBorder(), nums.getItemBoxHeight());
            g2d.fill(new RoundRectangle2D.Double(
                nums.getItemBoxX(),
                nums.getItemBoxOneY(),
                fm.stringWidth("To") + nums.getItemTextBoxExtra(),
                nums.getItemBoxHeight(),
                nums.getBorder() * 2,
                nums.getBorder() * 2
            ));

            textDrawer.setText("To");
            textDrawer.setPos(new int[]{nums.getItemTextX(), nums.getItemBoxOneY() + nums.getItemTextYOffset()});
            textDrawer.drawText();

            g2d.setPaint(Color.decode(colorConfig.get("mid")));
            g2d.fillRect(nums.getItemBoxX(), nums.getItemBoxThreeY(), nums.getBorder(), nums.getItemBoxHeight());
            g2d.fill(new RoundRectangle2D.Double(
                nums.getItemBoxX(),
                nums.getItemBoxThreeY(),
                fm.stringWidth("From") + nums.getItemTextBoxExtra(),
                nums.getItemBoxHeight(),
                nums.getBorder() * 2,
                nums.getBorder() * 2
            ));

            textDrawer.setText("From");
            textDrawer.setPos(new int[]{nums.getItemTextX(), nums.getItemBoxThreeY() + nums.getItemTextYOffset()});
            textDrawer.drawText();

            g2d.setPaint(Color.decode(colorConfig.get("mid")));
            g2d.fillRect(nums.getItemBoxX(), nums.getItemBoxFourY(), nums.getBorder(), nums.getItemBoxHeight());
            g2d.fill(new RoundRectangle2D.Double(
                nums.getItemBoxX(),
                nums.getItemBoxFourY(),
                fm.stringWidth(giftingUsername) + nums.getItemUserBoxExtra(),
                nums.getItemBoxHeight(),
                nums.getBorder() * 2,
                nums.getBorder() * 2
            ));

            textDrawer.setText(giftingUsername);
            textDrawer.setPos(new int[]{nums.getItemUserTagX(), nums.getItemBoxFourY() + nums.getItemTextYOffset()});
            textDrawer.drawText();

            GraphicsUtil.drawCircleImage(
                g2d,
                giftingUserAvatar,
                new int[]{nums.getItemUserAvatarX(), nums.getItemBoxFourY() + nums.getItemUserAvatarYOffset()},
                nums.getItemUserAvatarWidth()
            );
        }

        g2d.setPaint(Color.decode(colorConfig.get("mid")));
        g2d.fillRect(nums.getItemBoxX(), userBoxY, nums.getBorder(), nums.getItemBoxHeight());
        g2d.fill(new RoundRectangle2D.Double(
            nums.getItemBoxX(),
            userBoxY,
            fm.stringWidth(username) + nums.getItemUserBoxExtra(),
            nums.getItemBoxHeight(),
            nums.getBorder() * 2,
            nums.getBorder() * 2
        ));

        textDrawer.setText(username);
        textDrawer.setPos(new int[]{nums.getItemUserTagX(), userBoxY + nums.getItemTextYOffset()});
        textDrawer.drawText();

        GraphicsUtil.drawCircleImage(
            g2d,
            userAvatar,
            new int[]{nums.getItemUserAvatarX(), userBoxY + nums.getItemUserAvatarYOffset()},
            nums.getItemUserAvatarWidth()
        );

        if (this.bucks >= 0 && this.giftingUser == null) {
            g2d.setPaint(Color.decode(colorConfig.get("mid")));
            g2d.fillRect(nums.getItemBoxX(), nums.getItemBoxTwoY(), nums.getBorder(), nums.getItemBoxHeight());
            g2d.fill(new RoundRectangle2D.Double(
                nums.getItemBoxX(),
                nums.getItemBoxTwoY(),
                fm.stringWidth("+$" + bucks) + nums.getItemTextBoxExtra(),
                nums.getItemBoxHeight(),
                nums.getBorder() * 2,
                nums.getBorder() * 2
            ));

            textDrawer.setText("+%%bucks%%$" + bucks);
            textDrawer.setPos(new int[]{nums.getItemTextX(), nums.getItemBoxTwoY() + nums.getItemTextYOffset()});
            textDrawer.drawText();
        }

        return userDataImage.getSubimage(
            nums.getItemBoxX(),
            nums.getItemBoxOneY(),
            nums.getBigBoarSize()[0],
            nums.getItemBoxFourY() + nums.getItemBoxHeight() - nums.getItemBoxOneY()
        );
    }
}
