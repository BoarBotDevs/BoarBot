package dev.boarbot.api.bot;

import dev.boarbot.bot.config.BotConfig;
import net.dv8tion.jda.api.JDA;

public interface Bot {
    void create();
    JDA getJDA();
    void loadConfig();
    BotConfig getConfig();
    void deployCommands() throws IllegalAccessException;
}
