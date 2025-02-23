package dev.boarbot.bot.config.items;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class PowerupItemConfig extends BaseItemConfig {
    private String file = "";
    private String emoji = "";
    private Integer eventAmt = 0;
    private boolean marketable = false;
    private Map<String, OutcomeConfig> outcomes = new HashMap<>();
}
