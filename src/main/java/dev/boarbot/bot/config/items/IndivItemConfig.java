package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link IndivItemConfig IndivItemConfig.java}
 *
 * Stores an item configuration for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class IndivItemConfig {
    public String name = "";
    public String pluralName = "";
    public String description = "";
    public String file = "";
    public String staticFile;
    public String transparentColor;
    public Boolean isSB;
    public Boolean blacklisted;
    public Integer rewardAmt;
    public OutcomeConfig[] outcomes;
}
