package dev.boarbot.entities.boaruser.collectibles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectedPowerup {
    private long numTotal = 0;
    private long highestTotal = 0;
    private int numClaimed = 0;
    private int numUsed = 0;
    private Integer numActive;
    private Integer numOpened;
    private Long curOut;
    private Long lastOpened;
    private Integer numSuccess;
    private Integer[] raritiesUsed;
}
