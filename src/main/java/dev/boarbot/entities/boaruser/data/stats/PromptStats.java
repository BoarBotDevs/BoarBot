package dev.boarbot.entities.boaruser.data.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromptStats {
    private double avg = 100;
    private int attempts = 0;
}
