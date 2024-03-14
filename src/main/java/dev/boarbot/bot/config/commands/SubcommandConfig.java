package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link SubcommandConfig SubcommandConfig.java}
 *
 * Stores a specific subcommand configuration
 * for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class SubcommandConfig {
    public String name = "";
    public String description = "";

    // Arguments the command uses
    public SubcommandArgsConfig[] args = {};
}
