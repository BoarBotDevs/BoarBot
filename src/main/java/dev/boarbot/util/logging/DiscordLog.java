package dev.boarbot.util.logging;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class DiscordLog implements Configured {
    private static final TextChannel logChannel = BoarBotApp.getBot().getJDA()
        .getTextChannelById(CONFIG.getMainConfig().getLogChannel());
    private static boolean logsDisabled = logChannel == null;
    private static final StringBuilder curLogMessage = new StringBuilder();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void addLog(Class<?> clazz, String message) {
        if (logsDisabled || logChannel == null) {
            return;
        }

        if (curLogMessage.isEmpty()) {
            curLogMessage.append("```ansi");
        }

        String prefix = "\n[\033[0;34mINFO\033[0m] [%s] [%s]\n> "
            .formatted(LocalDateTime.now().format(formatter), clazz.getName());
        String fixedMessage = prefix + message + "\n";
        fixedMessage = fixedMessage.substring(0, Math.min(fixedMessage.length(), 1990));

        if (curLogMessage.length() + fixedMessage.length() + "```".length() > 2000) {
            sendLogs();
            addLog(clazz, message);
            return;
        }

        curLogMessage.append(fixedMessage);
    }

    private static void sendLogs() {
        if (logsDisabled || logChannel == null) {
            return;
        }

        if (curLogMessage.isEmpty()) {
            return;
        }

        curLogMessage.append("```");

        if (!curLogMessage.toString().equals("```ansi```") && !curLogMessage.toString().equals("```")) {
            sendMessage(curLogMessage.toString());
        }

        curLogMessage.setLength(0);
    }

    public static void forceLog(Class<?> clazz, String message) {
        addLog(clazz, message);
        sendLogs();
    }

    public static void sendException(Class<?> clazz, String message) {
        if (logsDisabled || logChannel == null) {
            return;
        }

        sendLogs();

        StringBuilder pings = new StringBuilder();

        for (String dev : CONFIG.getMainConfig().getDevs()) {
            pings.append("<@%s>".formatted(dev));
        }

        String prefix = "```ansi\n[\033[0;31mERROR\033[0m] [%s] [%s]\n> "
            .formatted(LocalDateTime.now().format(formatter), clazz.getName());
        message = pings + prefix + message + "\n";
        message = message.substring(0, Math.min(message.length(), 1997));
        message = message + "```";

        sendMessage(message);
    }

    public static void sendWarn(Class<?> clazz, String message) {
        if (logsDisabled || logChannel == null) {
            return;
        }

        sendLogs();

        String prefix = "```ansi\n[\033[0;33mWARN\033[0m] [%s] [%s]\n> "
            .formatted(LocalDateTime.now().format(formatter), clazz.getName());
        message = prefix + message + "\n";
        message = message.substring(0, Math.min(message.length(), 1997));
        message = message + "```";

        sendMessage(message);
    }

    private static void sendMessage(String message) {
        if (logsDisabled || logChannel == null) {
            return;
        }

        try {
            logChannel.sendMessage(message).queue(null, e ->
                Log.warn(null, DiscordLog.class, "Bot failed to send message to log channel.", e, true)
            );
        } catch (Exception exception) {
            if (exception instanceof InsufficientPermissionException) {
                logsDisabled = true;
                Log.warn(null, DiscordLog.class, "Bot cannot see log channel. Logs disabled.", exception, true);
                return;
            }

            Log.warn(null, DiscordLog.class, "Bot failed to send message to log channel.", exception, true);
        }
    }
}
