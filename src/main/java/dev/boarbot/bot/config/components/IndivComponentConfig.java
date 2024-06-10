package dev.boarbot.bot.config.components;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class IndivComponentConfig {
    private int type = 2;

    // Select menus
    private Integer min_values;
    private Integer max_values;
    private List<SelectOptionConfig> options;

    // Buttons
    private String label;
    private String url;
    private String emoji;

    // Select menus/Buttons
    private String custom_id;
    private boolean disabled = false;

    // Text inputs
    private Integer min_length = 0;
    private Integer max_length = 4000;
    private Boolean required = true;
    private String value;

    // Text inputs/Buttons
    private Integer style;

    // Text inputs/Select menus
    private String placeholder;
}
