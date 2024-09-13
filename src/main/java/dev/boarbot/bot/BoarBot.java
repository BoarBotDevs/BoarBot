package dev.boarbot.bot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.*;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.listeners.*;
import dev.boarbot.modals.ModalHandler;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class BoarBot implements Bot {
    private final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();
    private JDA jda;

    private final BotConfig config = new BotConfig();
    private Font font;

    private final Map<String, byte[]> byteCacheMap = new HashMap<>();
    private final Map<String, BufferedImage> imageCacheMap = new HashMap<>();

    private final Map<String, Constructor<? extends Subcommand>> subcommands = new HashMap<>();
    private final ConcurrentMap<String, Interactive> interactives = new ConcurrentHashMap<>();
    private final Map<String, ModalHandler> modalHandlers = new HashMap<>();

    private BotType botType;

    @Override
    public void create(BotType type) {
        this.botType = type;

        ConfigLoader.loadConfig();
        DatabaseLoader.loadIntoDatabase("rarities");
        DatabaseLoader.loadIntoDatabase("boars");
        DatabaseLoader.loadIntoDatabase("badges");
        CacheLoader.loadCache();

        this.jda = JDABuilder.createDefault(this.env.get("TOKEN"))
            .addEventListeners(
                new StopMessageListener(),
                new CommandListener(),
                new ComponentListener(),
                new ModalListener(),
                new ReadyListener()
            )
            .setActivity(Activity.customStatus("/boar help | boarbot.dev"))
            .build();

        CommandLoader.registerSubcommands();
    }

    @Override
    public BotType getBotType() {
        return this.botType;
    }

    @Override
    public JDA getJDA() {
        return this.jda;
    }

    @Override
    public Dotenv getEnv() {
        return this.env;
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
