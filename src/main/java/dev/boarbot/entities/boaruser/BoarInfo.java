package dev.boarbot.entities.boaruser;

import dev.boarbot.api.util.Configured;
import lombok.Getter;

import java.util.*;

@Getter
public class BoarInfo implements Comparable<BoarInfo>, Configured {
    private int amount;
    private final String rarityID;
    private long firstObtained = -1;
    private long lastObtained = -1;
    private final List<Long> editions = new ArrayList<>();
    private final List<Long> editionTimestamps = new ArrayList<>();

    private final static Map<String, Integer> rarityMap = new HashMap<>();

    static {
        int i = RARITIES.keySet().size()-1;
        for (String rarityID : RARITIES.keySet()) {
            BoarInfo.rarityMap.put(rarityID, i);
            i--;
        }
    }

    public BoarInfo(String rarityID) {
        this.rarityID = rarityID;
    }

    public void addEdition(long edition, long editionTimestamp) {
        this.editions.add(edition);
        this.editionTimestamps.add(editionTimestamp);
        this.amount++;

        if (this.firstObtained == -1 || editionTimestamp < this.firstObtained) {
            this.firstObtained = editionTimestamp;
        }

        if (this.lastObtained == -1 || editionTimestamp > this.lastObtained) {
            this.lastObtained = editionTimestamp;
        }
    }

    public int compareTo(BoarInfo otherObj) {
        return BoarInfo.rarityMap.get(this.rarityID).compareTo(BoarInfo.rarityMap.get(otherObj.rarityID));
    }

    public static Comparator<BoarInfo> amountComparator() {
        return Comparator.comparingInt(o -> o.amount);
    }

    public static Comparator<BoarInfo> recentComparator() {
        return Comparator.comparingLong(o -> o.lastObtained);
    }

    public static Comparator<BoarInfo> newestComparator() {
        return Comparator.comparingLong(o -> o.firstObtained);
    }

    public static Comparator<String> alphaComparator() {
        return Comparator.comparing(key -> BOARS.get(key).getName().toLowerCase());
    }
}
