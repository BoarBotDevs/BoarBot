package dev.boarbot.util.logging;

import dev.boarbot.listeners.ReadyListener;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {
    private static Logger log(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void debug(String message) {
        debug(null, Log.class, message);
    }

    public static void debug(Class<?> clazz, String message) {
        debug(null, clazz, message);
    }

    public static void debug(User user, Class<?> clazz, String message) {
        String msg = getUserMsg(user, message);
        log(clazz).debug(msg);
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
        String msg = getUserMsg(user, message);
        log(clazz).info(msg);

        if (!ReadyListener.isDone()) {
            return;
        }

        if (force) {
            DiscordLog.forceLog(clazz, msg);
            return;
        }

        DiscordLog.addLog(clazz, msg);
    }

    public static void warn(Class<?> clazz, String message) {
        warn(null, clazz, message, null);
    }

    public static void warn(Class<?> clazz, String message, Throwable exception) {
        warn(null, clazz, message, exception);
    }

    public static void warn(User user, Class<?> clazz, String message) {
        warn(user, clazz, message, null);
    }

    public static void warn(User user, Class<?> clazz, String message, Throwable exception) {
        String msg = getUserMsg(user, message);

        if (exception != null) {
            log(clazz).warn(msg, exception);
        } else {
            log(clazz).warn(msg);
        }

        if (!ReadyListener.isDone()) {
            return;
        }

        DiscordLog.sendWarn(clazz, msg);
    }

    public static void error(Class<?> clazz, String message, Throwable exception) {
        error(null, clazz, message, exception);
    }

    public static void error(User user, Class<?> clazz, String message, Throwable exception) {
        String msg = getUserMsg(user, message);
        log(clazz).error(msg, exception);

        if (!ReadyListener.isDone()) {
            return;
        }

        DiscordLog.sendException(clazz, msg);
    }

    public static String getUserSuffix(User user, String userID) {
        if (user != null) {
            return " %s (%s)".formatted(user.getName(), userID);
        }
        return " (%s)".formatted(userID);
    }

    private static String getUserMsg(User user, String message) {
        String userPart = user == null
            ? ""
            : "%s (%s) - ".formatted(user.getName(), user.getId());

        return "%s%s".formatted(userPart, message);
    }
}
