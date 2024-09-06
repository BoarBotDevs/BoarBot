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
    private String name = "";
    private String pluralName;
    private String emoji = "";
    private double weight = 0;
    private int baseScore = 0;
    private boolean fromDaily = false;
    private int chargesNeeded = 0;
    private int avgClones = 0;
    private boolean hunterNeed = false;
    private boolean givesFirstBoar = false;
    private boolean hidden = false;
    private boolean showEdition = false;
    private String[] boars = {};
}
