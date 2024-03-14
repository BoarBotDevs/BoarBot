package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link BoarManageCommandsConfig BoarManageCommandsConfig.java}
 *
 * Stores configurations for the {@link dev.boarbot.commands.BoarManageCommands boar-manage command}
 * for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class BoarManageCommandsConfig {
    /**
     * {@link SubcommandConfig Subcommand information} for {@link SetupSubcommand}
     */
    public SubcommandConfig setup = new SubcommandConfig();
}
