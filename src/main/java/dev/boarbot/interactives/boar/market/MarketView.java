package dev.boarbot.interactives.boar.market;

public enum MarketView {
    OVERVIEW("overview"),
    RARITIES("rarities"),
    ITEMS("items"),
    FOCUSED("focused");

    private final String type;

    MarketView(final String type) {
        this.type = type;
    }

    public static MarketView fromString(String str) {
        for (MarketView view : MarketView.values()) {
            if (view.type.equals(str)) {
                return view;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
