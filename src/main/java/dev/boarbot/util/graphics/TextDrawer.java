package dev.boarbot.util.graphics;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TextDrawer {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    @Getter @Setter private Graphics2D g2d;

    @Getter @Setter private String text;
    @Getter @Setter private int[] pos;
    @Getter @Setter private Align align;
    @Getter @Setter private String colorVal;
    @Getter private float fontSize;
    @Getter @Setter private int width;
    @Getter @Setter private boolean wrap;

    private final List<String> words = new ArrayList<>();
    private final List<String> colorValsList = new ArrayList<>();
    private final List<Integer> colorSwapIndexes = new ArrayList<>();

    public TextDrawer(
        Graphics2D g2d, String text, int[] pos, Align align, String colorVal, float fontSize
    ) {
        this(g2d, text, pos, align, colorVal, fontSize, -1, false);
    }

    public TextDrawer(
        Graphics2D g2d, String text, int[] pos, Align align, String colorVal, float fontSize, int width
    ) {
        this(g2d, text, pos, align, colorVal, fontSize, width, false);
    }

    public TextDrawer(
        Graphics2D g2d, String text, int[] pos, Align align, String colorVal, float fontSize, int width, boolean wrap
    ) {
        this.g2d = g2d;
        this.text = text;
        this.pos = pos;
        this.align = align;
        this.colorVal = colorVal;
        this.width = width;
        this.wrap = wrap;

        this.setFontSize(fontSize);
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        this.g2d.setFont(BoarBotApp.getBot().getFont().deriveFont(fontSize));
    }

    public double drawText() {
        this.words.clear();
        this.colorValsList.clear();
        this.colorSwapIndexes.clear();

        parseText();

        if (this.width == -1) {
            this.drawTextLine(String.join(" ", this.words), 0, this.pos);

            FontMetrics fm = this.g2d.getFontMetrics();
            return (fm.getAscent() + fm.getDescent()) * 1.1;
        }

        if (!this.wrap) {
            String parsedText = String.join(" ", this.words);

            while (this.g2d.getFontMetrics().stringWidth(parsedText) > this.width) {
                Font curFont = this.g2d.getFont();
                this.g2d.setFont(curFont.deriveFont(curFont.getSize() - 1f));
            }

            int[] newPos = new int[]{
                this.pos[0],
                this.pos[1] + (this.g2d.getFontMetrics().getAscent() + this.g2d.getFontMetrics().getDescent()) / 2
            };

            this.drawTextLine(parsedText, 0, newPos);

            FontMetrics fm = this.g2d.getFontMetrics();
            double lineHeight = (fm.getAscent() + fm.getDescent()) * 1.1;

            this.g2d.setFont(this.g2d.getFont().deriveFont(this.fontSize));

            return lineHeight;
        }

        List<String> lines = new ArrayList<>();
        List<Integer> lineStarts = new ArrayList<>();

        lineStarts.add(0);

        StringBuilder curLine = new StringBuilder();
        int curIndex = 0;

        FontMetrics fm = this.g2d.getFontMetrics();
        double newY = this.pos[1];
        double lineHeight = (fm.getAscent() + fm.getDescent()) * 1.1;

        for (String word : this.words) {
            if (curLine.isEmpty() || fm.stringWidth(curLine + word) <= this.width) {
                curLine.append(word).append(" ");
                curIndex += word.length();
                continue;
            }

            lines.add(curLine.deleteCharAt(curLine.length()-1).toString());
            lineStarts.add(curIndex);

            curIndex += word.length();

            curLine.setLength(0);
            curLine.append(word).append(" ");
        }

        if (!curLine.isEmpty()) {
            lines.add(curLine.deleteCharAt(curLine.length()-1).toString());
        } else {
            lines.removeLast();
        }

        newY -= lineHeight * (lines.size()-1) / 2;

        for (int i=0; i<lines.size(); i++) {
            int[] newPos = new int[]{this.pos[0], (int) newY};
            this.drawTextLine(lines.get(i), lineStarts.get(i), newPos);
            newY += lineHeight;
        }

        return lineHeight * (lines.size()-1);
    }

    private void parseText() {
        this.colorValsList.add(this.colorVal);

        char[] textArray = this.text.toCharArray();
        int startCurColorTag = -1;
        int wordsIndex = 0;

        StringBuilder curWord = new StringBuilder();

        int i=0;
        while (i < textArray.length) {
            char curChar = textArray[i];

            boolean inColorTag = startCurColorTag != -1;
            boolean isStartColorTag = i < textArray.length-1 && curChar == '%' && textArray[i+1] == '%' && !inColorTag;
            boolean isEndColorTag = i < textArray.length-1 && curChar == '%' && textArray[i+1] == '%' && inColorTag;

            if (isStartColorTag) {
                startCurColorTag = i += 2;
                continue;
            }

            if (isEndColorTag) {
                String colorKey = this.text.substring(startCurColorTag, i);
                String curColorVal = this.config.getColorConfig().get(colorKey);

                this.colorValsList.add(curColorVal);

                startCurColorTag = -1;
                i += 2;

                this.colorSwapIndexes.add(wordsIndex);

                continue;
            }

            boolean isWordChar = !inColorTag && !Character.isWhitespace(curChar);
            boolean isBetweenWords = !inColorTag && Character.isWhitespace(curChar);

            if (isWordChar) {
                wordsIndex++;
                curWord.append(curChar);
            }

            if (isBetweenWords && !curWord.isEmpty()) {
                this.words.add(curWord.toString());
                curWord.setLength(0);
            }

            i++;
        }

        if (!curWord.isEmpty()) {
            this.words.add(curWord.toString());
        }
    }

    private void drawTextLine(String textLine, int startIndex, int[] pos) {
        int curColorIndex = 0;

        for (Integer colorSwapIndex : this.colorSwapIndexes) {
            if (startIndex < colorSwapIndex) {
                break;
            }

            curColorIndex++;
        }

        int startCurSegment = 0;
        int numSpaces = 0;
        int[] adjustedPos = new int[]{pos[0], pos[1]};

        for (int i=0; i<textLine.length(); i++) {
            int actualIndex = startIndex + i - numSpaces;
            boolean notLastColor = curColorIndex < this.colorSwapIndexes.size();

            if (notLastColor && actualIndex >= this.colorSwapIndexes.get(curColorIndex)) {
                adjustedPos[0] = getPosAdjust(pos[0], textLine, startCurSegment);
                drawSegment(textLine.substring(startCurSegment, i), this.colorValsList.get(curColorIndex), adjustedPos);

                curColorIndex++;
                startCurSegment = i;
            }

            if (Character.isWhitespace(textLine.charAt(i))) {
                numSpaces++;
            }
        }

        adjustedPos[0] = getPosAdjust(pos[0], textLine, startCurSegment);
        drawSegment(textLine.substring(startCurSegment), this.colorValsList.get(curColorIndex), adjustedPos);
    }

    private int getPosAdjust(int x, String textLine, int startSegment) {
        FontMetrics fm = this.g2d.getFontMetrics();

        if (this.align == Align.LEFT) {
            return x + fm.stringWidth(textLine.substring(0, startSegment));
        } else if (this.align == Align.RIGHT) {
            return x - fm.stringWidth(textLine.substring(startSegment));
        } else {
            return x + fm.stringWidth(textLine.substring(0, startSegment)) / 2 -
                fm.stringWidth(textLine.substring(startSegment)) / 2;
        }
    }

    private void drawSegment(String segment, String colorStr, int[] pos) {
        String[] gradStringColors = colorStr.split(",");

        if (gradStringColors.length > 1) {
            List<Color> gradColors = new ArrayList<>();
            FontMetrics fm = this.g2d.getFontMetrics();

            for (String gradStringColor : gradStringColors) {
                gradColors.add(Color.decode(gradStringColor));
            }

            float[] colorStops = new float[gradStringColors.length];

            for (int i=0; i<colorStops.length; i++) {
                colorStops[i] = i / Math.max(colorStops.length-1, 1f);
            }

            BufferedImage textImage = new BufferedImage(
                fm.stringWidth(segment), fm.getAscent() + fm.getDescent(), BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D textG2D = textImage.createGraphics();

            textG2D.setFont(this.g2d.getFont().deriveFont(this.fontSize));

            textG2D.drawString(segment, 0, fm.getAscent());
            textG2D.setComposite(AlphaComposite.SrcIn);

            LinearGradientPaint paint = new LinearGradientPaint(
                0, 0, textImage.getWidth(), textImage.getHeight(), colorStops, gradColors.toArray(new Color[0])
            );

            textG2D.setPaint(paint);
            textG2D.fillRect(0, 0, textImage.getWidth(), textImage.getHeight());

            this.g2d.drawImage(textImage, pos[0], pos[1] - fm.getAscent(), null);
        } else {
            this.g2d.setColor(Color.decode(colorStr));
            this.g2d.drawString(segment, pos[0], pos[1]);
        }

        this.g2d.setColor(null);
    }
}
