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
public class SubcommandArgsConfig {
    public String name = "";
    public int type = -1;
    public String description = "";
    public Boolean required = false;
    public Boolean autocomplete = false;
    public ArgChoicesConfig<?>[] choices;

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
