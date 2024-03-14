package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link ColorConfig ColorConfig.java}
 *
 * Stores number configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class ColorConfig {
    // Colors used in custom images

    public String dark = "#000000";
    public String mid = "#000000";
    public String light = "#000000";
    public String font = "#000000";

    // Colors for different item types

    public String badge = "#000000";
    public String powerup = "#000000";
    public String bucks = "#000000";

    // Leaderboard colors

    public String gold = "#000000";
    public String silver = "#000000"; // Used for slight emphasis too
    public String bronze = "#000000";

    // General purpose colors

    public String green = "#000000";
    public String maintenance = "#000000";
    public String error = "#000000";

    // Boar rarity colors

    public String rarity1 = "#000000";
    public String rarity2 = "#000000";
    public String rarity3 = "#000000";
    public String rarity4 = "#000000";
    public String rarity5 = "#000000";
    public String rarity6 = "#000000";
    public String rarity7 = "#000000";
}
