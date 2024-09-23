package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoarData {
    private int num = 0;
    private int[] editions = {};
    private long[] editionDates = {};
    private long firstObtained = 0;
    private long lastObtained = 0;
}
