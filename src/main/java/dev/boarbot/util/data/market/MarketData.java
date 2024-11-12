package dev.boarbot.util.data.market;

import java.sql.Timestamp;

public record MarketData(int stock, long sellPrice, long buyPrice, Timestamp lastPurchase, Timestamp lastSell) {
    public MarketData() {
        this(0, 1, 2, null, null);
    }
}
