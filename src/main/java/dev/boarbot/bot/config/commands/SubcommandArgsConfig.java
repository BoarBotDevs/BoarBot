package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;

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
public class SubcommandArgsConfig {
    private String name = "";
    private int type = -1;
    private String description = "";
    private Boolean required = false;
    private Boolean autocomplete = false;
    private ArgChoicesConfig<?>[] choices;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "{\"name\":\"%s\",\"type\":%d,\"description\":\"%s\""
                .formatted(name, type, description)
        );

        if (required) {
            sb.append(",\"required\":%b".formatted(true));
        }

        if (autocomplete) {
            sb.append(",\"autocomplete\":%b".formatted(true));
        }

        if (choices != null) {
            for (int i=0; i<choices.length; i++) {
                ArgChoicesConfig<?> choice = choices[i];

                if (i == 0) {
                    sb.append(",\"choices\":[");
                }

                sb.append("%s".formatted(choice.toString()));

                if (i == choices.length-1) {
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
