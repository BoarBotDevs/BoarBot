package dev.boarbot.migration.globaldata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class OldMarketData {
    private Map<String, OldItemData> powerups = new HashMap<>();
    private Map<String, OldItemData> boars = new HashMap<>();
}
