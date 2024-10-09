package dev.boarbot.util.graphics;

import dev.boarbot.api.util.Configured;

public class TextUtil implements Configured {
    public static void drawLabel(TextDrawer td, String text, int[] pos) {
        TextUtil.drawLabel(td, text, pos, COLORS.get("font"));
    }

    public static void drawLabel(TextDrawer td, String text, int[] pos, String colorVal) {
        int mediumFont = NUMS.getFontMedium();

        td.setText(text);
        td.setPos(pos);
        td.setFontSize(mediumFont);
        td.setColorVal(colorVal);
        td.drawText();
    }

    public static void drawValue(TextDrawer td, String text, int[] pos) {
        TextUtil.drawValue(td, text, pos, false, COLORS.get("silver"));
    }

    public static void drawValue(TextDrawer td, String text, int[] pos, boolean sameFontSize) {
        TextUtil.drawValue(td, text, pos, sameFontSize, COLORS.get("silver"));
    }

    public static void drawValue(TextDrawer td, String text, int[] pos, boolean sameFontSize, String colorVal) {
        int mediumFont = NUMS.getFontMedium();
        int bigFont = NUMS.getFontBig();

        td.setText(text);
        td.setPos(pos);
        td.setFontSize(sameFontSize ? mediumFont : bigFont);
        td.setColorVal(colorVal);
        td.drawText();
    }

    public static String getFractionStr(int val, int max) {
        return val >= max
            ? "<>gold<>%,d<>silver<>/<>gold<>%,d".formatted(val, max)
            : "<>error<>%,d<>silver<>/<>green<>%,d".formatted(val, max);
    }

    public static String getBlessHex(long blessings, boolean miraclesActive) {
        if (miraclesActive || blessings > 1000) {
            return COLORS.get("powerup");
        }

        String[] bless1Colors = COLORS.get("blessing1").substring(1).split("(?<=\\G.{2})");
        String[] bless2Colors = COLORS.get("blessing2").substring(1).split("(?<=\\G.{2})");
        double blessPercent = blessings / (double) (
            NUMS.getMaxStreakBless() + NUMS.getMaxQuestBless() + NUMS.getMaxUniqueBless() + NUMS.getMaxOtherBless()
        );

        String blessRed = Integer.toHexString(
            (int) (Integer.parseInt(bless1Colors[0], 16) + blessPercent *
                (Integer.parseInt(bless2Colors[0], 16) - Integer.parseInt(bless1Colors[0], 16)))
        );
        blessRed = blessRed.length() == 1
            ? "0" + blessRed
            : blessRed;

        String blessGreen = Integer.toHexString(
            (int) (Integer.parseInt(bless1Colors[1], 16) + blessPercent *
                (Integer.parseInt(bless2Colors[1], 16) - Integer.parseInt(bless1Colors[1], 16)))
        );
        blessGreen = blessGreen.length() == 1
            ? "0" + blessGreen
            : blessGreen;

        String blessBlue = Integer.toHexString(
            (int) (Integer.parseInt(bless1Colors[2], 16) + blessPercent *
                (Integer.parseInt(bless2Colors[2], 16) - Integer.parseInt(bless1Colors[2], 16)))
        );
        blessBlue = blessBlue.length() == 1
            ? "0" + blessBlue
            : blessBlue;

        return "#" + blessRed + blessGreen + blessBlue;
    }
}
