package dev.boarbot.bot.config.prompts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndivPromptConfig {
    public String name = "";
    public String description = "";
    public String emoji1;
    public String emoji2;
    public String[] choices;
    public Integer numButtons;
    public String rightClock;
}
