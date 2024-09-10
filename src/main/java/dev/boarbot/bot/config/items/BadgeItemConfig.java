package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BadgeItemConfig extends BaseItemConfig {
    private String[] files = {};
    private String[] descriptions = {};
}
