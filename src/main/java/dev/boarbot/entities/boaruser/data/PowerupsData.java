package dev.boarbot.entities.boaruser.data;

import java.util.Map;

public record PowerupsData(long blessings, boolean miraclesActive, Map<String, Integer> powAmts) {}
