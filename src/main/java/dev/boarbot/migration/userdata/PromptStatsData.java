package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PromptStatsData {
    private double avg = 0;
    private int attempts = 0;
}
