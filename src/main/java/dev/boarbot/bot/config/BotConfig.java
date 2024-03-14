package dev.boarbot.bot.config;

import dev.boarbot.bot.config.prompts.PromptConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link BotConfig BotConfig.java}
 *
 * Stores configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class BotConfig {
    /**
     * All user IDs associated with developers
     */
    private String[] devs = new String[0];

    /**
     * The text channel ID the bot sends certain logs to
     */
    private String logChannel = "";

    /**
     * The forum channel ID the bot sends reports to
     */
    private String reportsChannel = "";

    /**
     * The text channel ID the bot sends update messages to
     */
    private String updatesChannel = "";

    /**
     * The text channel ID the bot defaults to for notifications
     */
    private String defaultChannel = "";

    /**
     * The text channel ID the channel to send spook messages to
     */
    private String spookChannel = "";

    /**
     * Boars can be obtained without waiting for the next day
     */
    private boolean unlimitedBoars = false;

    /**
     * Debug messages should be sent to logs
     */
    private boolean debugMode = true;

    /**
     * Bot is in maintenance mode
     */
    private boolean maintenanceMode = false;

    /**
     * Market can be opened using /boar market
     */
    private boolean marketOpen = false;

    /**
     * The {@link PathConfig paths} of all files/folders the bot accesses
     */
    private PathConfig pathConfig = new PathConfig();

    /**
     * {@link StringConfig String constants} the bot uses for responses and more
     */
    private StringConfig stringConfig = new StringConfig();

    /**
     * Non-intuitive number constants the bot uses
     */
    private NumberConfig numberConfig = new NumberConfig();

    /**
     * Collection of information about powerups
     */
    private PromptConfig promptConfig = new PromptConfig();

    /**
     * Collection of information about quests
     */
    private Map<String, QuestConfig> questConfig = new HashMap<>();

    /**
     * Collection of {@link CommandConfig command configurations} the bot uses
     */
    private CommandConfig commandConfig = new CommandConfig();

    /**
     * Collection of sets of item configurations
     */
    private ItemConfig itemConfig = new ItemConfig();

    /**
     * Array of {@link RarityConfig rarity configurations}
     */
    private RarityConfig[] rarityConfigs = new RarityConfig[0];

    /**
     * {@link ColorConfig Color configurations} used by the bot
     */
    private ColorConfig colorConfig = new ColorConfig();
}
