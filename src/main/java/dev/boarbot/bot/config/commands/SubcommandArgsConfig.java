package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link SubcommandArgsConfig SubcommandArgsConfig.java}
 *
 * Stores subcommand argument configurations for a bot
 * instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class SubcommandArgsConfig {
    public String name = "";
    public String description = "";
    public Boolean required;
    public Boolean autocomplete;
    public ArgChoicesConfig[] choices;
}
