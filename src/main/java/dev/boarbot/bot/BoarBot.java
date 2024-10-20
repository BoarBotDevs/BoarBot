package dev.boarbot.bot;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.*;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.listeners.*;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BoarBot implements Bot, Configured {
    private JDA jda;

    private final BotConfig config = new BotConfig();
    private Font font;

    private final Map<String, byte[]> byteCacheMap = new HashMap<>();
    private final Map<String, BufferedImage> imageCacheMap = new HashMap<>();

    private final Map<String, Constructor<? extends Subcommand>> subcommands = new HashMap<>();
    private final ConcurrentMap<String, Interactive> interactives = new ConcurrentHashMap<>();
    private final Map<String, ModalHandler> modalHandlers = new ConcurrentHashMap<>();

    @Override
    public void create() {
        Log.info(this.getClass(), "Starting up bot...");

        ConfigLoader.loadConfig();
        DataUtil.setupDatabase();
        DatabaseLoader.loadIntoDatabase();
        CacheLoader.loadCache();
        CommandLoader.registerSubcommands();

        this.jda = JDABuilder.createDefault(BoarBotApp.getEnv("TOKEN"))
            .addEventListeners(
                new StopMessageListener(),
                new CommandListener(),
                new ComponentListener(),
                new ModalListener(),
                new ReadyListener()
            )
            .setActivity(Activity.customStatus(STRS.getActivityStatus()))
            .setEnabledIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.SCHEDULED_EVENTS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES
            )
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .build();
    }

    @Override
    public JDA getJDA() {
        return this.jda;
    }

    @Override
    public BotConfig getConfig() {
        return this.config;
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public Font getFont() {
        return this.font;
    }

    @Override
    public void deployCommands() {
        CommandLoader.deployCommands();
    }

    @Override
    public Map<String, byte[]> getByteCacheMap() {
        return this.byteCacheMap;
    }

    @Override
    public Map<String, BufferedImage> getImageCacheMap() {
        return this.imageCacheMap;
    }

    @Override
    public Map<String, Constructor<? extends Subcommand>> getSubcommands() {
        return this.subcommands;
    }

    @Override
    public ConcurrentMap<String, Interactive> getInteractives() {
        return this.interactives;
    }

    @Override
    public Map<String, ModalHandler> getModalHandlers() { return this.modalHandlers; }
}
