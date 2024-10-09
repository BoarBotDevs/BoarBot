package dev.boarbot.entities.boaruser.data;

import java.sql.Timestamp;

public record ProfileData(
    String lastBoarID,
    long boarBucks,
    long totalBoars,
    int numDailies,
    Timestamp lastDailyTimestamp,
    int uniqueBoars,
    int numSkyblock,
    int streak,
    long blessings,
    int streakBless,
    int uniqueBless,
    int questBless,
    int otherBless,
    boolean miraclesActive
) {
    public ProfileData() {
        this(
            null,
            0,
            0,
            0,
            null,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            false
        );
    }
}
