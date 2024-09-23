package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestsStatsData {
    private int totalCompleted = 0;
    private int totalFullCompleted = 0;
}
