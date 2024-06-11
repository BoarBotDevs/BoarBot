package dev.boarbot.bot.config.components;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SelectOptionConfig {
    private String label = "";
    private String value = "";
    private String emoji;
    private String description;
}