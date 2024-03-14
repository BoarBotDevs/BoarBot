package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link BoarDevCommandsConfig BoarDevCommandsConfig}
 *
 * Stores configurations for the {@link dev.boarbot.commands.BoarDevCommands boar-dev command}
 * for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class BoarDevCommandsConfig {
    /**
     * {@link SubcommandConfig Subcommand information} for {@link GiveSubcommand}
     */
    public SubcommandConfig give = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link BanSubcommand}
     */
    public SubcommandConfig ban = new SubcommandConfig();
}
