package dev.boarbot.entities.boaruser.collectibles;

import dev.boarbot.bot.config.commands.SubcommandConfig;
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
