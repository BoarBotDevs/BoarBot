package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Arrays;

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
public class SubcommandConfig {
    private String name = "";
    private String description = "";
    private String location = "";

    // Arguments the command uses
    private SubcommandArgsConfig[] options;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"name\":\"%s\",\"type\":1,\"description\":\"%s\"".formatted(name, description));

        if (options != null) {
            for (int i=0; i<options.length; i++) {
                SubcommandArgsConfig option = options[i];

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
