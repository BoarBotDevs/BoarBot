package dev.boarbot.bot.config.prompts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndivPromptConfig {
    private String name = "";
    private String description = "";
    private String emoji1;
    private String emoji2;
    private String[] choices;
    private Integer numButtons;
    private String rightClock;
}
