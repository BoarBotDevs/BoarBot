package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;

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
public class ArgChoicesConfig<T> {
    private String name = "";
    private T value;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"name\":\"%s\",".formatted(name));

        if (value instanceof Integer) {
            sb.append("\"value\":%d".formatted(value));
        } else if (value instanceof Double) {
            sb.append("\"value\":%f".formatted(value));
        } else {
            sb.append("\"value\":\"%s\"".formatted(value));
        }

        sb.append("}");

        return sb.toString();
    }
}
