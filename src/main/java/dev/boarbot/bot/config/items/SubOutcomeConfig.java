package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link SubOutcomeConfig SubOutcomeConfig.java}
 *
 * Stores an outcome sub configuration for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class SubOutcomeConfig {
    public int weight = 0;
    public String name = "";
}
