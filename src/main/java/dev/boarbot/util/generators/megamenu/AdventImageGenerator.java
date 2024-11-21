package dev.boarbot.util.generators.megamenu;

import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.BadgeData;
import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class AdventImageGenerator extends MegaMenuGenerator {
    public AdventImageGenerator(int page, BoarUser boarUser, List<BadgeData> badges, String firstJoinedDate) {
        super(page, boarUser, badges, firstJoinedDate);
    }

    @Override
    public MegaMenuGenerator generate() throws IOException, URISyntaxException {
        this.generatedImage = new BufferedImage(IMAGE_SIZE[0], IMAGE_SIZE[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = generatedImage.createGraphics();

        GraphicsUtil.drawImage(g2d, ResourceUtil.adventUnderlayPath, ORIGIN, IMAGE_SIZE);

        this.drawTopInfo();
        return this;
    }
}
