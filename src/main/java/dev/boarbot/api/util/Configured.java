package dev.boarbot.api.util;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.*;
import dev.boarbot.bot.config.items.BadgeItemConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;

import java.util.Map;

public interface Configured {
    BotConfig CONFIG = BoarBotApp.getBot().getConfig();
    NumberConfig NUMS = CONFIG.getNumberConfig();
    StringConfig STRS = CONFIG.getStringConfig();
    PathConfig PATHS = CONFIG.getPathConfig();
    Map<String, String> COLORS = CONFIG.getColorConfig();
    Map<String, BoarItemConfig> BOARS = CONFIG.getItemConfig().getBoars();
    Map<String, BadgeItemConfig> BADGES = CONFIG.getItemConfig().getBadges();
    Map<String, PowerupItemConfig> POWS = CONFIG.getItemConfig().getPowerups();
    Map<String, RarityConfig> RARITIES = CONFIG.getRarityConfigs();
}
