package dev.boarbot.entities.boaruser.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PowerupStats {
    private int attempts = 0;
    private int oneAttempts = 0;
    private int fastestTime = 0;
    private Map<String, Map<String, PromptStats>> prompts = new HashMap<>();
}
