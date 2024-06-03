package dev.boarbot.util.time;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

public final class TimeUtil {
    public static long getLastDailyResetMilli() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getNextDailyResetMilli() {
        BotConfig config = BoarBotApp.getBot().getConfig();
        return TimeUtil.getLastDailyResetMilli() + config.getNumberConfig().getOneDay();
    }

    public static long getCurMilli() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static boolean isHalloween() {
        LocalDateTime curDateTime = LocalDateTime.now();
        return curDateTime.getMonth() == Month.OCTOBER && curDateTime.getDayOfMonth() >= 24;
    }

    public static boolean isChristmas() {
        LocalDateTime curDateTime = LocalDateTime.now();
        return curDateTime.getMonth() == Month.DECEMBER && curDateTime.getDayOfMonth() >= 24;
    }
}
