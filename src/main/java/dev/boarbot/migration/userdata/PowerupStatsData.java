package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class PowerupStatsData {
    private int attempts = 0;
    private int oneAttempts = 0;
    private int fastestTime = 0;
    private Map<String, Map<String, PromptStatsData>> prompts = new HashMap<>();
}
