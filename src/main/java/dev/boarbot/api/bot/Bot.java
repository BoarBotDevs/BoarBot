package dev.boarbot.api.bot;

import dev.boarbot.bot.BotType;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.modals.ModalHandler;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.Map;

public interface Bot {
    void create(BotType type);
    BotType getBotType();
    JDA getJDA();
    void loadConfig();
    BotConfig getConfig();
    Font getFont();
    Map<String, byte[]> getByteCacheMap();
    Map<String, BufferedImage> getImageCacheMap();
    void deployCommands();
    void registerSubcommands();
    Map<String, Constructor<? extends Subcommand>> getSubcommands();
    Map<String, Interactive> getInteractives();
    Map<String, ModalHandler> getModalHandlers();
}