package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PowerupItemConfig extends BaseItemConfig {
    private Integer rewardAmt;
    private OutcomeConfig[] outcomes;
}
