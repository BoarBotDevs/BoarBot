package dev.boarbot.api.bot;

import dev.boarbot.bot.BotType;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.commands.Subcommand;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.Map;

public interface Bot {
    void create(BotType type);
    BotType getBotType();
    JDA getJDA();
    void loadConfig();
    BotConfig getConfig();
    Font getFont();
    Map<String, byte[]> getCacheMap();
    void deployCommands();
    void registerSubcommands();
    Map<String, Constructor<? extends Subcommand>> getSubcommands();
    Map<String, Interactive> getInteractives();
}
