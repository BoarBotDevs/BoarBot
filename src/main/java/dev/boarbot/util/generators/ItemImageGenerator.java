package dev.boarbot.util.generators;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.*;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;

@Log4j2
public class ItemImageGenerator {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    private final User user;
    private final String title;

    @Getter @Setter private String itemName;
    @Getter @Setter private String filePath;
    @Getter @Setter private String staticFilePath;
    @Getter @Setter private String colorKey;
    @Getter @Setter private User giftingUser;
    @Getter @Setter private long bucks;

    private byte[] generatedImageByteArray;

    public ItemImageGenerator(User user, String title, String itemID) {
        this(user, title, itemID, false);
    }

    public ItemImageGenerator(User user, String title, String itemID, boolean isBadge) {
        this(user, title, itemID, isBadge, null);
    }

    public ItemImageGenerator(User user, String title, String itemID, boolean isBadge, User giftingUser) {
        this(user, title, itemID, isBadge, giftingUser, 0);
    }

    public ItemImageGenerator(User user, String title, String itemID, boolean isBadge, User giftingUser, long bucks) {
        this.user = user;
        this.title = title;

        if (isBadge) {
            IndivItemConfig badgeInfo = this.config.getItemConfig().getBadges().get(itemID);
            this.itemName = badgeInfo.name;
            this.filePath = this.config.getPathConfig().getBadges() + badgeInfo.file;
            this.staticFilePath = null;
            this.colorKey = "badge";
        } else {
            IndivItemConfig boarInfo = this.config.getItemConfig().getBoars().get(itemID);
            this.itemName = boarInfo.name;
            this.filePath = this.config.getPathConfig().getBoars() + boarInfo.getFile();
            this.staticFilePath = boarInfo.getStaticFile() != null
                ? this.config.getPathConfig().getBoars() + boarInfo.getStaticFile()
                : null;
            this.colorKey = BoarUtil.findRarityKey(itemID);
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
        this(user, title, itemName, filePath, colorKey, giftingUser, 0);
    }

    public ItemImageGenerator(
        User user, String title, String itemName, String filePath, String colorKey, User giftingUser, long bucks
    ) {
        this.user = user;
        this.title = title;

        this.itemName = itemName;
        this.filePath = filePath;
        this.staticFilePath = null;
        this.colorKey = colorKey;

        this.giftingUser = giftingUser;
        this.bucks = bucks;
    }

    public byte[] generate() throws IOException, URISyntaxException {
        return this.generate(false);
    }

    public byte[] generate(boolean forceStatic) throws IOException, URISyntaxException {
        String extension = this.filePath.split("[.]")[1];

        if (extension.equals("gif") && !forceStatic) {
            this.generatedImageByteArray = BoarBotApp.getBot().getCacheMap().get(
                "animitem" + this.title.toLowerCase().replaceAll("[^a-z]+", "") + this.itemName + this.colorKey
            );

            if (this.generatedImageByteArray == null) {
                this.generateStatic(false);
                this.generateAnimated();

                BoarBotApp.getBot().getCacheMap().put(
                    "animitem" + this.title.toLowerCase().replaceAll("[^a-z]+", "") + this.itemName + this.colorKey,
                    this.generatedImageByteArray
                );
            }

            this.addAnimatedUser();
        } else {
            this.generatedImageByteArray = BoarBotApp.getBot().getCacheMap().get(
                "item" + this.title.toLowerCase().replaceAll("[^a-z]+", "") + this.itemName + this.colorKey
            );

            if (this.generatedImageByteArray == null) {
                this.generateStatic(true);

                BoarBotApp.getBot().getCacheMap().put(
                    "item" + this.title.toLowerCase().replaceAll("[^a-z]+", "") + this.itemName + this.colorKey,
                    this.generatedImageByteArray
                );
            }

            this.addStaticUser();
        }

        return this.generatedImageByteArray;
    }

    private void generateAnimated() throws IOException {
        Gson g = new Gson();

        Process pythonProcess = new ProcessBuilder(
            "python",
            "src/main/resources/scripts/make_animated_image.py",
            g.toJson(this.config.getPathConfig()),
            g.toJson(this.config.getNumberConfig()),
            this.filePath
        ).start();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
        BufferedReader stdErr = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));
        OutputStream stdOut = pythonProcess.getOutputStream();

        stdOut.write(this.generatedImageByteArray);
        stdOut.close();

        String result = stdIn.readLine();

        if (result == null) {
            String tempErrMessage;
            String errMessage = "";

            while ((tempErrMessage = stdErr.readLine()) != null) {
                errMessage = errMessage.concat(tempErrMessage + "\n");
            }

            log.error(errMessage);
        }

        this.generatedImageByteArray = Base64.getDecoder().decode(result);
    }

    private void addAnimatedUser() throws IOException {
        Gson g = new Gson();

        Process pythonProcess = new ProcessBuilder(
            "python",
            "src/main/resources/scripts/user_animated_overlay.py",
            g.toJson(this.config.getPathConfig()),
            g.toJson(this.config.getColorConfig()),
            g.toJson(this.config.getNumberConfig()),
            this.user.getEffectiveAvatarUrl(),
            this.user.getName(),
            "%,d".formatted(this.bucks),
            this.giftingUser == null ? "" : this.giftingUser.getEffectiveAvatarUrl(),
            this.giftingUser == null ? "" : this.giftingUser.getName()
        ).start();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
        BufferedReader stdErr = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));
        OutputStream stdOut = pythonProcess.getOutputStream();

        stdOut.write(this.generatedImageByteArray);
        stdOut.close();

        String result = stdIn.readLine();

        if (result == null) {
            String tempErrMessage;
            String errMessage = "";

            while ((tempErrMessage = stdErr.readLine()) != null) {
                errMessage = errMessage.concat(tempErrMessage + "\n");
            }

            log.error(errMessage);
        }

        this.generatedImageByteArray = Base64.getDecoder().decode(result);
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
            String filePath = this.staticFilePath != null ? this.staticFilePath : this.filePath;
            GraphicsUtil.drawImage(g2d, filePath, mainPos, mainSize);
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

            g2d.setPaint(Color.decode(colorConfig.get("dark")));
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

            g2d.setPaint(Color.decode(colorConfig.get("dark")));
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

            g2d.setPaint(Color.decode(colorConfig.get("dark")));
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

        g2d.setPaint(Color.decode(colorConfig.get("dark")));
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

        if (this.bucks > 0 && this.giftingUser == null) {
            String formattedBucks = "%,d".formatted(this.bucks);

            g2d.setPaint(Color.decode(colorConfig.get("dark")));
            g2d.fillRect(nums.getItemBoxX(), nums.getItemBoxTwoY(), nums.getBorder(), nums.getItemBoxHeight());
            g2d.fill(new RoundRectangle2D.Double(
                nums.getItemBoxX(),
                nums.getItemBoxTwoY(),
                fm.stringWidth("+$" + formattedBucks) + nums.getItemTextBoxExtra(),
                nums.getItemBoxHeight(),
                nums.getBorder() * 2,
                nums.getBorder() * 2
            ));

            textDrawer.setText("+//bucks//$" + formattedBucks);
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
