package dev.boarbot.util.graphics;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TextDrawer {
    private final Graphics2D g2d;

    @Getter @Setter private String text;
    @Getter @Setter private int[] pos;
    @Getter @Setter private Align align;
    @Getter @Setter private String colorStr;
    @Getter private float fontSize;
    @Getter @Setter private BotConfig config;
    @Getter @Setter private int width;
    @Getter @Setter private boolean wrap;

    private final List<String> words = new ArrayList<>();
    private final List<Color> colorsList = new ArrayList<>();
    private final List<Integer> colorSwapIndexes = new ArrayList<>();

    public TextDrawer(
        Graphics2D g2d, String text, int[] pos, Align align, String colorStr, float fontSize, BotConfig config
    ) {
        this(g2d, text, pos, align, colorStr, fontSize, config, -1, false);
    }

    public TextDrawer(
        Graphics2D g2d, String text, int[] pos, Align align, String colorStr, float fontSize, BotConfig config, int width
    ) {
        this(g2d, text, pos, align, colorStr, fontSize, config, width, false);
    }

    public TextDrawer(
        Graphics2D g2d,
        String text,
        int[] pos,
        Align align,
        String colorStr,
        float fontSize,
        BotConfig config,
        int width,
        boolean wrap
    ) {
        this.g2d = g2d;
        this.text = text;
        this.pos = pos;
        this.align = align;
        this.colorStr = colorStr;
        this.config = config;
        this.width = width;
        this.wrap = wrap;

        this.setFontSize(fontSize);
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        this.g2d.setFont(BoarBotApp.getBot().getFont().deriveFont(fontSize));
    }

    public void drawText() {
        this.words.clear();
        this.colorsList.clear();
        this.colorSwapIndexes.clear();

        parseText();

        this.g2d.setColor(this.colorsList.getFirst());

        if (this.width == -1) {
            this.drawTextLine(String.join(" ", this.words), 0, this.pos);

            this.g2d.setColor(null);
            return;
        }

        if (!this.wrap) {
            // Shrink text to fit width

            this.g2d.setColor(null);
            return;
        }

        // Wrap text to fit width

        this.g2d.setColor(null);
    }

    private void parseText() {
        this.colorsList.add(Color.decode(this.colorStr));

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
                Color curColor = Color.decode(this.config.getColorConfig().get(colorKey));

                this.colorsList.add(curColor);

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
                drawSegment(textLine.substring(startCurSegment, i), this.colorsList.get(curColorIndex), adjustedPos);

                curColorIndex++;
                startCurSegment = i;
            }

            if (Character.isWhitespace(textLine.charAt(i))) {
                numSpaces++;
            }
        }

        adjustedPos[0] = getPosAdjust(pos[0], textLine, startCurSegment);
        drawSegment(textLine.substring(startCurSegment), this.colorsList.get(curColorIndex), adjustedPos);
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

    private void drawSegment(String segment, Color color, int[] pos) {
        this.g2d.setColor(color);
        this.g2d.drawString(segment, pos[0], pos[1]);
        this.g2d.setColor(null);
    }
}
