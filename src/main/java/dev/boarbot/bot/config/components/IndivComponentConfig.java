package dev.boarbot.bot.config.components;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;

@Getter
@Setter
@ToString
public class IndivComponentConfig {
    private int type = 2;
    private String custom_id;
    private boolean disabled = false;

    // Select menus
    private String placeholder;
    private Integer min_values;
    private Integer max_values;
    private List<SelectOption> options;

    // Buttons
    private String label;
    private Integer style;
    private String url;
    private String emoji;
}
