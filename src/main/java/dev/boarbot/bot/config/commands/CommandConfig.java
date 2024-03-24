package dev.boarbot.bot.config.commands;

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
    public String name = "";
    public String description = "";
    public Integer default_member_permissions;
    public SubcommandConfig[] options = {};

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"name\":\"%s\",\"description\":\"%s\"".formatted(name, description));

        if (default_member_permissions != null) {
            sb.append(",\"default_member_permissions\":%d".formatted(default_member_permissions));
        }

        if (options != null) {
            for (int i=0; i<options.length; i++) {
                SubcommandConfig option = options[i];

                if (i == 0) {
                    sb.append(",\"options\":[");
                }

                sb.append("%s".formatted(option.toString()));

                if (i == options.length-1) {
                    sb.append("]");
                    continue;
                }

                sb.append(",");
            }
        }

        sb.append("}");

        return sb.toString();
    }
}
