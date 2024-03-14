package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link OutcomeConfig OutcomeConfig.java}
 *
 * Stores an outcome configuration for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class OutcomeConfig {
    public int weight = 0;
    public String category = "";
    public SubOutcomeConfig[] subOutcomes = {};
}
