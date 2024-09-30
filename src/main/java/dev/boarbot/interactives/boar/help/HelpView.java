package dev.boarbot.interactives.boar.help;

public enum HelpView {
    GENERAL("general"),
    BOARS("boars"),
    BADGES("badges"),
    POWERUPS("powerups");

    private final String type;

    HelpView(final String type) {
        this.type = type;
    }

    public static HelpView fromString(String str) {
        for (HelpView view : HelpView.values()) {
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
