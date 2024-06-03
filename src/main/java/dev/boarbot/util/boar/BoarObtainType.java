package dev.boarbot.util.boar;

public enum BoarObtainType {
    DAILY("DAILY"),
    CLONE("CLONE"),
    TRANSMUTE("TRANSMUTE"),
    GIFT("GIFT"),
    OTHER("OTHER");

    private final String type;

    BoarObtainType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
