package dev.boarbot.bot.config.prompts;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link PromptConfig PromptTypeConfig.java}
 *
 * Stores a powerup prompt type configuration for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
public class PromptConfig {
    private String name = "";
    private String description = "";
    private int rightStyle = 0;
    private int wrongStyle = 0;
    private Map<String, IndivPromptConfig> prompts = new HashMap<>();
}
