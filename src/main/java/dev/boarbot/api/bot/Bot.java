package dev.boarbot.api.bot;

import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.commands.Subcommand;
import net.dv8tion.jda.api.JDA;

import java.lang.reflect.Constructor;
import java.util.Map;

public interface Bot {
    void create();
    JDA getJDA();
    void loadConfig();
    BotConfig getConfig();
    void deployCommands();
    void registerSubcommands();
    Map<String, Constructor<? extends Subcommand>> getSubcommands();
}
