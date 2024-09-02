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
    int highestStreak,
    int blessings,
    int highestBlessings,
    int streakBless,
    int highestStreakBless,
    int questBless,
    int highestQuestBless,
    int uniqueBless,
    int highestUniqueBless,
    int otherBless,
    int highestOtherBless
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
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
        );
    }
}
