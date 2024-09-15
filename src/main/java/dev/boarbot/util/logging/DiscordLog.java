package dev.boarbot.util.logging;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DiscordLog implements Configured {
    private static final TextChannel logChannel = BoarBotApp.getBot().getJDA()
        .getTextChannelById(CONFIG.getMainConfig().getLogChannel());
    private static boolean logsDisabled = logChannel == null;
    private static final StringBuilder curLogMessage = new StringBuilder();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void addLog(Class<?> clazz, String message) {
        addLog(null, clazz, message);
    }

    public static void addLog(User user, Class<?> clazz, String message) {
        String userPart = user == null
            ? ""
            : "%s (%s) - ".formatted(user.getName(), user.getId());
        message = "%s%s".formatted(userPart, message);

        log.info(message);

        if (logsDisabled || logChannel == null) {
            return;
        }

        if (curLogMessage.isEmpty()) {
            curLogMessage.append("```ansi");
        }

        String prefix = "\n[\033[0;34mINFO\033[0m] [%s] [%s]\n> "
            .formatted(LocalDateTime.now().format(formatter), clazz.getName());
        message = prefix + message + "\n";
        message = message.substring(0, Math.min(message.length(), 1990));

        if (curLogMessage.length() + message.length() + "```".length() > 2000) {
            sendLogs();
            curLogMessage.append("```ansi");
        }

        curLogMessage.append(message);
    }

    private static void sendLogs() {
        curLogMessage.append("```");

        if (logsDisabled || logChannel == null) {
            return;
        }

        try {
            logChannel.sendMessage(curLogMessage.toString()).queue();
        } catch (InsufficientPermissionException exception) {
            logsDisabled = true;
            log.warn("Bot does not have permission to send messages to log channel. Channel logs are disabled!");
        }

        curLogMessage.setLength(0);

    }

    public static void forceLog(Class<?> clazz, String message) {
        forceLog(null, clazz, message);
    }

    public static void forceLog(User user, Class<?> clazz, String message) {
        addLog(user, clazz, message);
        sendLogs();
    }
}
