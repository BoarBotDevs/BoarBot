package dev.boarbot.entities.boaruser;

public record StatsData(
    long bucks,
    long highestBucks,
    int dailies,
    int dailiesMissed,
    long lastDailyTimestamp,
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
            0,
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
