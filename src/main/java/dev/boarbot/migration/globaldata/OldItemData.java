package dev.boarbot.migration.globaldata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OldItemData {
    private OldBuySellData[] buyers = {};
    private OldBuySellData[] sellers = {};
}
