package dev.boarbot.util.data.top;

public enum TopType {
    TOTAL_BUCKS("total_bucks"),
    TOTAL_BOARD("total_boars"),
    UNIQUES("unique_boars"),
    STREAK("boar_streak"),
    PERFECTS("powerup_perfects"),
    GIFTS_SENT("gift"),
    CHARGES_USED("transmute"),
    BLESSINGS("blessings"),
    PEAK_BLESSINGS("highest_blessings"),
    FASTEST_POWERUP("powerup_fastest_time");

    private final String type;

    TopType(final String type) {
        this.type = type;
    }

    public static TopType fromString(String str) {
        for (TopType topType : TopType.values()) {
            if (topType.type.equals(str)) {
                return topType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
