package dev.boarbot.bot.config.items;

import dev.boarbot.api.util.Weighted;
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
public class SubOutcomeConfig implements Weighted {
    private int weight = 0;
    private Integer minBucks;
    private Integer maxBucks;
    private Integer rewardAmt;
}
