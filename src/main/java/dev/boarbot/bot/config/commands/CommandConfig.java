package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link CommandConfig CommandConfig.java}
 *
 * Stores all configurations for all commands for a bot
 * instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class CommandConfig {
    /**
     * {@link CommandConfig Command information} for {@link dev.boarbot.commands.BoarCommands BoarCommands}
     */
    public BoarCommandsConfig boar = new BoarCommandsConfig();

    /**
     * {@link CommandConfig Command information} for {@link dev.boarbot.commands.BoarDevCommands BoarDevCommands}
     */
    public BoarDevCommandsConfig boarDev = new BoarDevCommandsConfig();

    /**
     * {@link CommandConfig Command information} for {@link dev.boarbot.commands.BoarManageCommands BoarManageCommands}
     */
    public BoarManageCommandsConfig boarManage = new BoarManageCommandsConfig();
}
