package dev.boarbot.util.boar;

public enum BoarTag {
    DAILY("DAILY"),
    EXTRA("EXTRA"),
    CLONE("CLONE"),
    TRANSMUTE("TRANSMUTE"),
    GIFT("GIFT"),
    REWARD("REWARD"),
    GIVEN("GIVEN"),
    MARKET("MARKET");

    private final String type;

    BoarTag(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
