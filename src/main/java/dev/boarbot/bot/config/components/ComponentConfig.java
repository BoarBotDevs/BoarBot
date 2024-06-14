package dev.boarbot.bot.config.components;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class ComponentConfig {
    private Map<String, IndivComponentConfig> setup = new HashMap<>();
    private Map<String, IndivComponentConfig> daily = new HashMap<>();
    private Map<String, IndivComponentConfig> megaMenu = new HashMap<>();
}
