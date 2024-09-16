package dev.boarbot.util.logging;

import dev.boarbot.listeners.ReadyListener;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {
    private static Logger log(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void debug(Class<?> clazz, String message) {
        debug(null, clazz, message);
    }

    public static void debug(User user, Class<?> clazz, String message) {
        message = getUserMsg(user, message);
        log(clazz).debug(message);
    }

    public static void info(Class<?> clazz, String message) {
        info(null, clazz, message, false);
    }

    public static void info(User user, Class<?> clazz, String message) {
        info(user, clazz, message, false);
    }

    public static void info(Class<?> clazz, String message, boolean force) {
        info(null, clazz, message, force);
    }

    public static void info(User user, Class<?> clazz, String message, boolean force) {
        message = getUserMsg(user, message);
        log(clazz).info(message);

        if (!ReadyListener.isDone()) {
            return;
        }

        if (force) {
            DiscordLog.forceLog(clazz, message);
            return;
        }

        DiscordLog.addLog(clazz, message);
    }

    public static void warn(Class<?> clazz, String message) {
        warn(null, clazz, message, null);
    }

    public static void warn(Class<?> clazz, String message, Exception exception) {
        warn(null, clazz, message, exception);
    }

    public static void warn(User user, Class<?> clazz, String message) {
        warn(user, clazz, message, null);
    }

    public static void warn(User user, Class<?> clazz, String message, Exception exception) {
        message = getUserMsg(user, message);

        if (exception != null) {
            log(clazz).warn(message, exception);
        } else {
            log(clazz).warn(message);
        }


        if (!ReadyListener.isDone()) {
            return;
        }

        DiscordLog.sendWarn(clazz, message);
    }

    public static void error(Class<?> clazz, String message, Exception exception) {
        error(null, clazz, message, exception);
    }

    public static void error(User user, Class<?> clazz, String message, Exception exception) {
        message = getUserMsg(user, message);
        log(clazz).error(message, exception);

        if (!ReadyListener.isDone()) {
            return;
        }

        DiscordLog.sendException(clazz, message);
    }

    private static String getUserMsg(User user, String message) {
        String userPart = user == null
            ? ""
            : "%s (%s) - ".formatted(user.getName(), user.getId());

        return "%s%s".formatted(userPart, message);
    }
}
