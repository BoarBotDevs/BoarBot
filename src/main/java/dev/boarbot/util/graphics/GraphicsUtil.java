package dev.boarbot.util.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

public final class GraphicsUtil {
    public static void drawRect(Graphics2D g2d, int[] pos, int[] size, String color) {
        String[] gradStringColors = color.split(",");

        if (gradStringColors.length > 1) {
            List<Color> gradColors = new ArrayList<>();

            for (String gradStringColor : gradStringColors) {
                gradColors.add(Color.decode(gradStringColor));
            }

            float[] colorStops = new float[gradStringColors.length];

            for (int i=0; i<colorStops.length; i++) {
                colorStops[i] = i / Math.max(colorStops.length-1, 1f);
            }

            LinearGradientPaint paint = new LinearGradientPaint(
                pos[0], pos[1], pos[0] + size[0], pos[1] + size[1], colorStops, gradColors.toArray(new Color[0])
            );

            g2d.setPaint(paint);
        } else {
            g2d.setColor(Color.decode(color));
        }

        g2d.fillRect(pos[0], pos[1], size[0], size[1]);

        g2d.setPaint(null);
        g2d.setColor(null);
    }

    public static void drawImage(
        Graphics2D g2d, String path, int[] pos
    ) throws IOException, URISyntaxException {
        g2d.drawImage(GraphicsUtil.getImage(path), pos[0], pos[1], null);
    }

    public static void drawImage(
        Graphics2D g2d, String path, int[] pos, int[] size
    ) throws IOException, URISyntaxException {
        g2d.drawImage(GraphicsUtil.getImage(path), pos[0], pos[1], size[0], size[1], null);
    }

    public static Image getImage(String path) throws URISyntaxException, IOException {
        Image image;

        if (path.startsWith("http")) {
            image = ImageIO.read(new URI(path).toURL());
        } else {
            image = ImageIO.read(new FileInputStream(path));
        }

        return image;
    }

    public static void drawCircleImage(
        Graphics2D g2d, String path, int[] pos, int diameter
    ) throws IOException, URISyntaxException {
        g2d.setClip(new RoundRectangle2D.Double(
            pos[0], pos[1], diameter, diameter, diameter, diameter
        ));

        drawImage(g2d, path, pos, new int[]{diameter, diameter});

        g2d.setClip(null);
    }
}
