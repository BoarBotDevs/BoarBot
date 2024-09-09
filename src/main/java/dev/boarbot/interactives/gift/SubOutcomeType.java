package dev.boarbot.interactives.gift;

public enum SubOutcomeType {
    SPECIAL_SANTA("santa"),
    SPECIAL_UNDERWEAR("underwear"),
    BUCKS_SMALL("small"),
    BUCKS_MEDIUM("medium"),
    BUCKS_LARGE("large"),
    POWERUP_CLONE("clone"),
    POWERUP_MIRACLE("miracle"),
    POWERUP_TRANSMUTE("transmute");

    private final String type;

    SubOutcomeType(final String type) {
        this.type = type;
    }

    public static SubOutcomeType fromString(String str) {
        for (SubOutcomeType outcomeType : SubOutcomeType.values()) {
            if (outcomeType.type.equals(str)) {
                return outcomeType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
