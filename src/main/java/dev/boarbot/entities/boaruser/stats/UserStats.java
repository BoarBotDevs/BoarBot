package dev.boarbot.entities.boaruser.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStats {
    private GeneralStats general = new GeneralStats();
    private PowerupStats powerups = new PowerupStats();
    private QuestStats quests = new QuestStats();
}
