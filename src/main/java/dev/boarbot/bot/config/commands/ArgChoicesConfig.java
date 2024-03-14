package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link ArgChoicesConfig ArgChoicesConfig.java}
 *
 * Stores choice configurations for a subcommand argument
 * for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class ArgChoicesConfig<T> {
    public String name = "";
    public T value;
}
