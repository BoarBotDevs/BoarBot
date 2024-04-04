package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;

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
public class CommandConfig {
    private String name = "";
    private String description = "";
    private Integer default_member_permissions;
    private Map<String, SubcommandConfig> subcommands = new HashMap<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"name\":\"%s\",\"description\":\"%s\"".formatted(name, description));

        if (default_member_permissions != null) {
            sb.append(",\"default_member_permissions\":%d".formatted(default_member_permissions));
        }

        int i=-1;
        for (SubcommandConfig subcommand : subcommands.values()) {
            i++;

            if (i == 0) {
                sb.append(",\"options\":[");
            }

            sb.append("%s".formatted(subcommand.toString()));

            if (i == subcommands.size()-1) {
                sb.append("]");
                continue;
            }

            sb.append(",");
        }

        sb.append("}");

        return sb.toString();
    }
}
