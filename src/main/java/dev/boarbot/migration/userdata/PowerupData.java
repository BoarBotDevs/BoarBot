package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PowerupData {
    private int numTotal = 0;
    private int highestTotal = 0;
    private int numUsed = 0;
    private Integer numOpened;
    private Integer[] raritiesUsed;
    private Integer numActive;
}
