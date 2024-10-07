package dev.boarbot.migration.globaldata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OldBuySellData {
    private String userID = "";
    private int num = 0;
    private int price = 0;
    private long[] editionDates = {};
    private int filledAmount = 0;
    private int claimedAmount = 0;
}
