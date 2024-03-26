package dev.boarbot.entities.boaruser.collectibles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectedBoar {
    private int num = 0;
    private int[] editions = {};
    private long[] editionDates = {};
    private long firstObtained = 0;
    private long lastObtained = 0;
}
