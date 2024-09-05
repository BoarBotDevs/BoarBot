package dev.boarbot.util.graphics;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;

import java.util.Map;

public class TextUtil {
    final static BotConfig config = BoarBotApp.getBot().getConfig();
    final static Map<String, String> colorConfig = config.getColorConfig();
    final static NumberConfig nums = config.getNumberConfig();

    public static void drawLabel(TextDrawer td, String text, int[] pos) {
        TextUtil.drawLabel(td, text, pos, colorConfig.get("font"));
    }

    public static void drawLabel(TextDrawer td, String text, int[] pos, String colorVal) {
        int mediumFont = nums.getFontMedium();

        td.setText(text);
        td.setPos(pos);
        td.setFontSize(mediumFont);
        td.setColorVal(colorVal);
        td.drawText();
    }

    public static void drawValue(TextDrawer td, String text, int[] pos) {
        TextUtil.drawValue(td, text, pos, false, colorConfig.get("silver"));
    }

    public static void drawValue(TextDrawer td, String text, int[] pos, boolean sameFontSize) {
        TextUtil.drawValue(td, text, pos, sameFontSize, colorConfig.get("silver"));
    }

    public static void drawValue(TextDrawer td, String text, int[] pos, boolean sameFontSize, String colorVal) {
        int mediumFont = nums.getFontMedium();
        int bigFont = nums.getFontBig();

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

    public static String getBlessHex(long blessings) {
        if (blessings > 1000) {
            return colorConfig.get("powerup");
        }

        String[] bless1Colors = colorConfig.get("blessing1").substring(1).split("(?<=\\G.{2})");
        String[] bless2Colors = colorConfig.get("blessing2").substring(1).split("(?<=\\G.{2})");
        double blessPercent = blessings / (double) (
            nums.getMaxStreakBless() + nums.getMaxQuestBless() + nums.getMaxUniqueBless() + nums.getMaxOtherBless()
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
