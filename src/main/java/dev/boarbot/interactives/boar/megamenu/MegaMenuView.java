package dev.boarbot.interactives.boar.megamenu;

public enum MegaMenuView {
    PROFILE("profile"),
    COLLECTION("collection"),
    COMPENDIUM("compendium"),
    STATS("stats"),
    POWERUPS("powerups"),
    QUESTS("quests"),
    BADGES("badges");

    private final String type;

    MegaMenuView(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
