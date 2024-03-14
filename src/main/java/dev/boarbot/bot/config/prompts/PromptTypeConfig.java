package dev.boarbot.bot.config.prompts;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link PromptTypeConfig PromptTypeConfig.java}
 *
 * Stores a powerup prompt type configuration for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
public class PromptTypeConfig {
    // TODO: Rework prompts then implement this

    private String name = "";
    private String description = "";
    private int rightStyle = 0;
    private int wrongStyle = 0;
}
