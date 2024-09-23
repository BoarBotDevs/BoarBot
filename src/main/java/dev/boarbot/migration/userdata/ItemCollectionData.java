package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class ItemCollectionData {
    private Map<String, BoarData> boars = new HashMap<>();
    private Map<String, BadgeData> badges = new HashMap<>();
    private Map<String, PowerupData> powerups = new HashMap<>();
}
