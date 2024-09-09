package dev.boarbot.bot.config;

import dev.boarbot.bot.config.commands.CommandConfig;
import dev.boarbot.bot.config.components.ComponentConfig;
import dev.boarbot.bot.config.items.ItemConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.bot.config.prompts.PromptConfig;
import dev.boarbot.bot.config.quests.QuestConfig;
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
     * The {@link MainConfig main configurations} for developers
     */
    private MainConfig mainConfig = new MainConfig();

    /**
     * Collection of {@link CommandConfig command configurations} the bot uses
     */
    private Map<String, CommandConfig> commandConfig = new HashMap<>();

    /**
     * Collection of component configurations the bot uses
     */
    private ComponentConfig componentConfig = new ComponentConfig();

    /**
     * Collection of modal configurations the bot uses
     */
    private Map<String, ModalConfig> modalConfig = new HashMap<>();

    /**
     * Collection of information about powerups
     */
    private Map<String, PromptConfig> promptConfig = new HashMap<>();

    /**
     * Collection of information about quests
     */
    private Map<String, QuestConfig> questConfig = new HashMap<>();

    /**
     * Array of {@link RarityConfig rarity configurations}
     */
    private Map<String, RarityConfig> rarityConfigs = new HashMap<>();

    /**
     * Collection of sets of item configurations
     */
    private ItemConfig itemConfig = new ItemConfig();

    /**
     * {@link StringConfig String constants} the bot uses for responses and more
     */
    private StringConfig stringConfig = new StringConfig();

    /**
     * Color configurations used by the bot
     */
    private Map<String, String> colorConfig = new HashMap<>();

    /**
     * Non-intuitive number constants the bot uses
     */
    private NumberConfig numberConfig = new NumberConfig();

    /**
     * The {@link PathConfig paths} of all files/folders the bot accesses
     */
    private PathConfig pathConfig = new PathConfig();
}
