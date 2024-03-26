package dev.boarbot.entities.boaruser.collectibles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectedBadge {
    private boolean possession = false;
    private long firstObtained = 0;
    private long curObtained = 0;
    private long lastLost = 0;
    private int timesLost = 0;
}
