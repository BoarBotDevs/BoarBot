package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

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
    public Map<String, Object> boar = new HashMap<>();

    /**
     * {@link CommandConfig Command information} for {@link dev.boarbot.commands.BoarDevCommands BoarDevCommands}
     */
    public Map<String, Object> boarDev = new HashMap<>();

    /**
     * {@link CommandConfig Command information} for {@link dev.boarbot.commands.BoarManageCommands BoarManageCommands}
     */
    public Map<String, Object> boarManage = new HashMap<>();
}
