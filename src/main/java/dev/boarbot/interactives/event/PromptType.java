package dev.boarbot.interactives.event;

public enum PromptType {
    EMOJI_FIND("emojiFind"),
    TRIVIA("trivia"),
    FAST("fast"),
    CLOCK("clock");

    private final String type;

    PromptType(final String type) {
        this.type = type;
    }

    public static PromptType fromString(String str) {
        for (PromptType promptType : PromptType.values()) {
            if (promptType.type.equals(str)) {
                return promptType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
