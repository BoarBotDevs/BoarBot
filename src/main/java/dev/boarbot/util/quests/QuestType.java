package dev.boarbot.util.quests;

public enum QuestType {
    DAILY("daily"),
    COLLECT_RARITY("collectRarity"),
    SPEND_BUCKS("spendBucks"),
    COLLECT_BUCKS("collectBucks"),
    CLONE_BOARS("cloneBoars"),
    CLONE_RARITY("cloneRarity"),
    SEND_GIFTS("sendGifts"),
    OPEN_GIFTS("openGifts"),
    POW_WIN("powWin"),
    POW_FAST("powFast");

    private final String type;

    QuestType(final String type) {
        this.type = type;
    }

    public static QuestType fromString(String str) {
        for (QuestType questType : QuestType.values()) {
            if (questType.type.equals(str)) {
                return questType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
