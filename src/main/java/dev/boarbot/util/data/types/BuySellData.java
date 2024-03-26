package dev.boarbot.util.data.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuySellData {
    private String userID = "";
    private int price = 0;
    private int num = 0;
    private int[] editions = {};
    private long[] editionDates = {};
    private long listTime = 0;
    private int filledAmount = 0;
    private int claimedAmount = 0;
}
