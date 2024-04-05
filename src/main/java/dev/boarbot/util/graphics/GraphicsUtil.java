package dev.boarbot.util.graphics;

import dev.boarbot.bot.config.BotConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
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

    public static void drawImage(Graphics2D g2d, String filePath, int[] pos, int[] size) throws IOException {
        g2d.drawImage(ImageIO.read(new File(filePath)), pos[0], pos[1], size[0], size[1], null);
    }

    public static void drawText(
        Graphics2D g2d, String text, int[] pos, Align align, String colorStr, BotConfig config
    ) {
        Color color = Color.decode(colorStr);

//        g2d.setFont(font);
        g2d.setColor(color);

        LinkedList<String> segments = new LinkedList<>();
        LinkedList<Color> segmentColors = new LinkedList<>();

        segmentColors.add(color);

        char[] textArray = text.toCharArray();
        int startCurColorTag = -1;
        int startSegment = 0;

        for (int i=0; i<textArray.length; i++) {
            if (i < textArray.length-1 && textArray[i] == '%' && textArray[i+1] == '%' && startCurColorTag == -1) {
                String curSegment = text.substring(startSegment, i);
                segments.add(curSegment);

                startCurColorTag = i+2;
                i++;

                continue;
            }

            if (i < textArray.length-1 && textArray[i] == '%' && textArray[i+1] == '%') {
                String colorKey = text.substring(startCurColorTag, i);
                Color curColor = Color.decode(config.getColorConfig().get(colorKey));

                segmentColors.add(curColor);
                startCurColorTag = -1;
                i++;
                startSegment = i+1;

                continue;
            }

            if (i == textArray.length-1) {
                segments.add(text.substring(startSegment));
            }
        }

        StringBuilder noTagText = new StringBuilder();
        int[] colorSwapIndexes = new int[segments.size()-1];

        int curSegmentIndex = 0;
        for (String segment : segments) {
            noTagText.append(segment);

            if (curSegmentIndex < segments.size()-1) {
                colorSwapIndexes[curSegmentIndex] = noTagText.length();
            }

            curSegmentIndex++;
        }

        g2d.drawString(text, pos[0], pos[1]);

        g2d.setColor(null);
    }
}
