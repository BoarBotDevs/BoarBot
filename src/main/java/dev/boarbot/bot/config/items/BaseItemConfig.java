package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class BaseItemConfig {
    private String name = "";
    private String pluralName;
    private String file = "";
}
