package dev.boarbot.util.data.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemData {
    private Integer curEdition;
    private BuySellData[] buyers = {};
    private BuySellData[] sellers = {};
    private int bestBuyPrice = 0;
    private int lastBestBuyPrice = 0;
    private String bestBuyUser = "";
    private int bestSellPrice = 0;
    private int lastBestSellPrice = 0;
    private String bestSellUser = "";
}
