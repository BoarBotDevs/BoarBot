package dev.boarbot.util.time;

import lombok.Getter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public final class TimeUtil {
    private static final double YEAR_MILLI = 1000 * 60 * 60 * 24 * 365.2422;
    private static final double MONTH_MILLI = 1000L * 60 * 60 * 24 * (365.2422 / 12);
    private static final double DAY_MILLI = 1000L * 60 * 60 * 24;
    private static final double HOUR_MILLI = 1000 * 60 * 60;
    private static final double MINUTE_MILLI = 1000 * 60;
    private static final double SECOND_MILLI = 1000;

    @Getter
    private final static DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
        .appendPattern("MMMM d, yyyy")
        .toFormatter();

    @Getter
    private final static DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
        .appendPattern("MMMM d, yyyy h:mm:ss a")
        .toFormatter();

    public static long getLastDailyResetMilli() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getNextDailyResetMilli() {
        return TimeUtil.getLastDailyResetMilli() + 1000 * 60 * 60 * 24;
    }

    public static long getCurMilli() {
        return Instant.now().toEpochMilli();
    }

    public static long getOneDayMilli() {
        return 1000 * 60 * 60 * 24;
    }

    public static long getLastQuestResetMilli() {
        return TimeUtil.getQuestResetMilli() - TimeUtil.getOneDayMilli() * 7;
    }

    public static long getQuestResetMilli() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1).toLocalDate().atStartOfDay();
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        int daysToAdd = dayOfWeek == 7
            ? dayOfWeek
            : 7 - dayOfWeek;
        dateTime = dateTime.plusDays(daysToAdd).minusMinutes(1);

        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static boolean isHalloween() {
        ZonedDateTime curDateTime = ZonedDateTime.now(ZoneId.of("America/Chicago"));
        return curDateTime.getMonth() == Month.OCTOBER && curDateTime.getDayOfMonth() >= 26 ||
            curDateTime.getMonth() == Month.NOVEMBER && curDateTime.getDayOfMonth() <= 2;
    }

    public static boolean isChristmas() {
        ZonedDateTime curDateTime = ZonedDateTime.now(ZoneId.of("America/Chicago"));
        return curDateTime.getMonth() == Month.DECEMBER &&
            curDateTime.getDayOfMonth() >= 19 && curDateTime.getDayOfMonth() <= 26;
    }

    public static int getYear() {
        return LocalDate.now().getYear();
    }

    public static String getTimeDistance(long milli, boolean shortened) {
        return TimeUtil.getTimeDistance(TimeUtil.getCurMilli(), milli, shortened);
    }

    public static String getTimeDistance(long start, long milli, boolean shortened) {
        long millisDistance = milli - start;
        boolean isInPast = millisDistance < 0;
        millisDistance = Math.abs(millisDistance);

        int years = (int) (millisDistance / YEAR_MILLI);
        int months = (int) (millisDistance / MONTH_MILLI);
        int days = (int) (millisDistance / DAY_MILLI);
        int hours = (int) ((millisDistance + 1000 * 60 * 30) / HOUR_MILLI);
        int minutes = (int) (millisDistance / MINUTE_MILLI);
        int seconds = (int) (millisDistance / SECOND_MILLI);

        String distanceStr;

        if (shortened) {
            distanceStr = isInPast
                ? "-%,d%s"
                : "%,d%s";
        } else {
            distanceStr = isInPast
                ? "%,d %s ago"
                : "in %,d %s";
        }

        int valueToReturn = years;
        String valueType = shortened
            ? "y"
            : "year";

        if (seconds <= 60) {
            valueToReturn = seconds;
            valueType = shortened
                ? "s"
                : "second";
        } else if (minutes <= 60) {
            valueToReturn = minutes;
            valueType = shortened
                ? "m"
                : "minute";
        } else if (hours <= 24) {
            valueToReturn = hours;
            valueType = shortened
                ? "h"
                : "hour";
        } else if (days <= 365.2422 / 12) {
            valueToReturn = days;
            valueType = shortened
                ? "d"
                : "day";
        } else if (months <= 12) {
            valueToReturn = months;
            valueType = shortened
                ? "mo"
                : "month";
        }

        if (!shortened) {
            valueType = valueToReturn == 1
                ? valueType
                : valueType + "s";
        }

        return distanceStr.formatted(valueToReturn, valueType);
    }
}
