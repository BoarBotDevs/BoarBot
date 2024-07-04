package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link RarityConfig RarityConfig.ts}
 *
 * Stores a specific rarity configuration
 * for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class RarityConfig {
    public String name = "";
    public String pluralName = "";
    public String emoji = "";
    public double weight = 0;
    public int baseScore = 0;
    public boolean fromDaily = false;
    public int enhancersNeeded = 0;
    public int avgClones = 0;
    public boolean hunterNeed = false;
    public boolean givesSpecial = false;
    public boolean hidden = false;
    public String[] boars = {};
}
