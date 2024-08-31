package dev.boarbot.interactives.boar.megamenu;

public enum InteractType {
    FAVORITE("favorite"),
    CLONE("clone"),
    TRANSMUTE("transmute"),
    ANIMATE("animate"),
    EDITIONS("editions"),
    ZOOM("zoom");

    private final String type;

    InteractType(final String type) {
        this.type = type;
    }

    public static InteractType fromString(String str) {
        for (InteractType interactType : InteractType.values()) {
            if (interactType.type.equals(str)) {
                return interactType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
