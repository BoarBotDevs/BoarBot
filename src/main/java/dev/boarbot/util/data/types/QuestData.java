package dev.boarbot.util.data.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestData {
    private long questsStartTimestamp = 0;
    private String[] curQuestIDs = {"","","","","","",""};
}
