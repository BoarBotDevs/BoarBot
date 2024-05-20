package dev.boarbot.entities.boaruser.data.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestStats {
    private long questWeekStart = 0;
    private int[] progress = new int[7];
    private int[] claimed = new int[8];
    private int totalCompleted = 0;
    private int totalFullCompleted = 0;
}
