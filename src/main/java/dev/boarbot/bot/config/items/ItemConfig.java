package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ItemConfig ItemConfig.java}
 *
 * Stores all item configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class ItemConfig {
    private Map<String, BoarItemConfig> boars = new HashMap<>();
    private Map<String, BadgeItemConfig> badges = new HashMap<>();
    private Map<String, PowerupItemConfig> powerups = new HashMap<>();
}
