package dev.boarbot.entities.boaruser;

import java.util.ArrayList;
import java.util.List;

public record QuestData(
    List<Integer> questProgress,
    List<Boolean> questClaims,
    int questsCompleted,
    int perfectWeeks,
    boolean fullClaimed
) {
    public QuestData() {
        this(new ArrayList<>(), new ArrayList<>(), 0, 0, false);
    }
}
