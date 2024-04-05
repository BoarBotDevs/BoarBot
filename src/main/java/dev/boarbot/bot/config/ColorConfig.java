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

    private String dark = "#000000";
    private String mid = "#000000";
    private String light = "#000000";
    private String font = "#000000";

    // Colors for different item types

    private String badge = "#000000";
    private String powerup = "#000000";
    private String bucks = "#000000";

    // Leaderboard colors

    private String gold = "#000000";
    private String silver = "#000000"; // Used for slight emphasis too
    private String bronze = "#000000";

    // General purpose colors

    private String green = "#000000";
    private String maintenance = "#000000";
    private String error = "#000000";

    // Boar rarity colors

    private String rarity1 = "#000000";
    private String rarity2 = "#000000";
    private String rarity3 = "#000000";
    private String rarity4 = "#000000";
    private String rarity5 = "#000000";
    private String rarity6 = "#000000";
    private String rarity7 = "#000000";
    private String rarity8 = "#000000";
    private String rarity9 = "#000000";
    private String rarity10 = "#000000";
    private String rarity11 = "#000000";
    private String rarity12 = "#000000";
}
