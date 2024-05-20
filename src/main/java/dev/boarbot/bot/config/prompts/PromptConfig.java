package dev.boarbot.bot.config.prompts;

import dev.boarbot.bot.config.components.ComponentConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link PromptConfig PromptConfig.java}
 *
 * Stores powerup configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
public class PromptConfig {
    private Map<String, PromptTypeConfig> types = new HashMap<>();
    private ComponentConfig[] rows = {};
}
