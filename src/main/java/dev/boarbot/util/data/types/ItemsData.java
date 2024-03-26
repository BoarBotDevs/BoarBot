package dev.boarbot.util.data.types;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ItemsData {
    private Map<String, ItemData> powerups = new HashMap<>();
    private Map<String, ItemData> boars = new HashMap<>();
}
