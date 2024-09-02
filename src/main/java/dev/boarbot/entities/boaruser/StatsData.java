package dev.boarbot.entities.boaruser;

import java.sql.Timestamp;

public record StatsData(
    long bucks,
    long highestBucks,
    int dailies,
    int dailiesMissed,
    Timestamp lastDailyTimestamp,
    String lastBoar,
    String favBoar,
    long totalBoars,
    long highestBoars,
    int uniques,
    int highestUniques,
    int boarStreak,
    int highestStreak
) {
    public StatsData() {
        this(
            0,
            0,
            0,
            0,
            null,
            null,
            null,
            0,
            0,
            0,
            0,
            0,
            0
        );
    }
}
