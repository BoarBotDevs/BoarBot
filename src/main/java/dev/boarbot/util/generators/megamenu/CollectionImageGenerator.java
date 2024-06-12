package dev.boarbot.util.generators.megamenu;

import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.graphics.Align;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.graphics.TextDrawer;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class CollectionImageGenerator extends MegaMenuGenerator {
    private final Map<String, BoarInfo> boarInfos;

    public CollectionImageGenerator(
        int page, BoarUser boarUser, List<String> badgeIDs, String firstJoinedDate, Map<String, BoarInfo> boarInfos
    ) {
        super(page, boarUser, badgeIDs, firstJoinedDate);
        this.boarInfos = boarInfos;
    }

    public FileUpload generate() throws IOException, URISyntaxException {
        int[] origin = {0, 0};
        int[] imageSize = this.nums.getCollImageSize();
        int[] boarImageSize = this.nums.getMediumBoarSize();

        String underlayPath = this.pathConfig.getCollAssets() + this.pathConfig.getCollUnderlay();

        this.generatedImage = new BufferedImage(imageSize[0], imageSize[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, underlayPath, origin, imageSize);

        String[] boarIDs = this.boarInfos.keySet().toArray(new String[0]);
        int boarsPerPage = 15;
        int lastBoarIndex = Math.min((this.page+1)*boarsPerPage, boarIDs.length);

        for (int i=this.page*boarsPerPage; i<lastBoarIndex; i++) {
            String boarID = boarIDs[i];
            int relativeIndex = i - this.page*boarsPerPage;

            String boarFile = this.itemConfig.getBoars().get(boarID).getStaticFile() != null
                ? this.itemConfig.getBoars().get(boarID).getStaticFile()
                : this.itemConfig.getBoars().get(boarID).getFile();
            String boarPath = this.pathConfig.getBoars() + boarFile;
            int[] boarPos = new int[] {
                this.nums.getCollBoarStartX() + (relativeIndex % 5) * (boarImageSize[0] + this.nums.getBorder()),
                this.nums.getCollBoarStartY() + (relativeIndex / 5) * (boarImageSize[1] + this.nums.getBorder())
            };

            int[] amountPos = new int[] {boarPos[0] + 15, boarPos[1] + 49};
            String amount = "%,d".formatted(Math.min(this.boarInfos.get(boarID).amount(), this.nums.getMaxBoars()));
            TextDrawer textDrawer = new TextDrawer(
                g2d, amount, amountPos, Align.LEFT, this.colorConfig.get("font"), this.nums.getFontMedium()
            );
            FontMetrics fm = g2d.getFontMetrics();
            int[] rectangleSize = new int[] {
                fm.stringWidth(amount) + 27,
                this.nums.getCollRarityHeight()
            };

            int[] rarityPos = new int[] {boarPos[0] + rectangleSize[0], boarPos[1]};
            int[] raritySize = new int[] {7, this.nums.getCollRarityHeight()};
            String color = this.colorConfig.get(this.boarInfos.get(boarID).rarityID());

            GraphicsUtil.drawImage(g2d, boarPath, boarPos, boarImageSize);
            GraphicsUtil.drawRect(g2d, boarPos, rectangleSize, this.colorConfig.get("dark"));
            textDrawer.drawText();
            GraphicsUtil.drawRect(g2d, rarityPos, raritySize, color);
        }

        this.drawTopInfo();
        return this.getFileUpload();
    }
}
