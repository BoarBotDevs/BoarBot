package dev.boarbot.entities.boaruser.data;

import java.util.HashMap;
import java.util.Map;

public record PowerupsData(long blessings, Map<String, Integer> powAmts) {
    public PowerupsData() {
        this(0, new HashMap<>());
    }
}
