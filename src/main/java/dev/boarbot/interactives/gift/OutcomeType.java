package dev.boarbot.interactives.gift;

public enum OutcomeType {
    SPECIAL("special"),
    BUCKS("bucks"),
    POWERUP("powerup"),
    BOAR("boar");

    private final String type;

    OutcomeType(final String type) {
        this.type = type;
    }

    public static OutcomeType fromString(String str) {
        for (OutcomeType outcomeType : OutcomeType.values()) {
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
