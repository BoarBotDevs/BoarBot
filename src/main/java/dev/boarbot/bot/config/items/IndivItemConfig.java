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
    public String pluralName;
    public String file = "";
    public String staticFile;
    public String description = "";
    public String classification = "None";
    public String update = "v1.0.0";
    public String[] searchTerms = new String[0];
    public boolean isSB = false;
    public boolean blacklisted = false;
    public boolean secret = false;
    public Integer rewardAmt;
    public OutcomeConfig[] outcomes;
}
