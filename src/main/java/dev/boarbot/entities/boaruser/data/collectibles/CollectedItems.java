package dev.boarbot.entities.boaruser.data.collectibles;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CollectedItems {
    private Map<String, CollectedBoar> boars = new HashMap<>();
    private Map<String, CollectedBadge> badges = new HashMap<>();
    private Map<String, CollectedPowerup> powerups = new HashMap<>();
    private Map<String, CollectedTheme> themes = new HashMap<>();
}
