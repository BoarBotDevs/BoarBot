package dev.boarbot.util.time;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;

import java.time.*;

public final class TimeUtil {
    private static final double YEAR_MILLI = 1000 * 60 * 60 * 24 * 365.2422;
    private static final double MONTH_MILLI = 1000L * 60 * 60 * 24 * (365.2422 / 12);
    private static final double DAY_MILLI = 1000L * 60 * 60 * 24;
    private static final double HOUR_MILLI = 1000 * 60 * 60;
    private static final double MINUTE_MILLI = 1000 * 60;
    private static final double SECOND_MILLI = 1000;

    public static long getLastDailyResetMilli() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getNextDailyResetMilli() {
        BotConfig config = BoarBotApp.getBot().getConfig();
        return TimeUtil.getLastDailyResetMilli() + config.getNumberConfig().getOneDay();
    }

    public static long getCurMilli() {
        return Instant.now().toEpochMilli();
    }

    public static boolean isHalloween() {
        LocalDateTime curDateTime = LocalDateTime.now();
        return curDateTime.getMonth() == Month.OCTOBER && curDateTime.getDayOfMonth() >= 24;
    }

    public static boolean isChristmas() {
        LocalDateTime curDateTime = LocalDateTime.now();
        return curDateTime.getMonth() == Month.DECEMBER && curDateTime.getDayOfMonth() >= 24;
    }

    public static String getTimeDistance(long milli) {
        long millisDistance = milli - TimeUtil.getCurMilli();
        boolean isInPast = millisDistance < 0;
        millisDistance = Math.abs(millisDistance);

        int years = (int) (millisDistance / YEAR_MILLI);
        int months = (int) (millisDistance / MONTH_MILLI);
        int days = (int) (millisDistance / DAY_MILLI);
        int hours = (int) (millisDistance / HOUR_MILLI);
        int minutes = (int) (millisDistance / MINUTE_MILLI);
        int seconds = (int) (millisDistance / SECOND_MILLI);

        String distanceStr = isInPast
            ? "%,d %s ago"
            : "in %,d %s";

        int valueToReturn = seconds;
        String valueType = "second";

        if (years > 0) {
            valueToReturn = years;
            valueType = "year";
        } else if (months > 0) {
            valueToReturn = months;
            valueType = "month";
        } else if (days > 0) {
            valueToReturn = days;
            valueType = "day";
        } else if (hours > 0) {
            valueToReturn = hours;
            valueType = "hour";
        } else if (minutes > 0) {
            valueToReturn = minutes;
            valueType = "minute";
        }

        return distanceStr.formatted(valueToReturn, valueToReturn == 1 ? valueType : valueType + "s");
    }
}
