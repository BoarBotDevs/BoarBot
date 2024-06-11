package dev.boarbot.interactives.boar.collection;

public enum CollectionView {
    PROFILE("profile"),
    COLLECTION("collection"),
    COMPENDIUM("compendium"),
    STATS("stats"),
    POWERUPS("powerups"),
    QUESTS("quests");

    private final String type;

    CollectionView(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
