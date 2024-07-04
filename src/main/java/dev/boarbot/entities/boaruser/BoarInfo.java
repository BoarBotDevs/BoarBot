package dev.boarbot.entities.boaruser;

import dev.boarbot.BoarBotApp;

import java.util.HashMap;
import java.util.Map;

public record BoarInfo(
    int amount, String rarityID, String firstObtained, String lastObtained
) implements Comparable<BoarInfo> {
    private final static Map<String, Integer> rarityMap = new HashMap<>();

    static {
        int i = BoarBotApp.getBot().getConfig().getRarityConfigs().keySet().size()-1;
        for (String rarityID : BoarBotApp.getBot().getConfig().getRarityConfigs().keySet()) {
            BoarInfo.rarityMap.put(rarityID, i);
            i--;
        }
    }

    public int compareTo(BoarInfo otherObj) {
        return BoarInfo.rarityMap.get(this.rarityID).compareTo(BoarInfo.rarityMap.get(otherObj.rarityID));
    }
}
