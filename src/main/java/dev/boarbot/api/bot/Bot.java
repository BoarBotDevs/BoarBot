package dev.boarbot.api.bot;

import net.dv8tion.jda.api.JDA;

public interface Bot {
    void create();
    JDA getJDA();
    void loadConfig();
}
