package dev.boarbot.bot.config.items;

import dev.boarbot.api.util.Weighted;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

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
public class OutcomeConfig implements Weighted {
    private int weight = 0;
    private String rewardStr;
    private Map<String, SubOutcomeConfig> subOutcomes;
}
