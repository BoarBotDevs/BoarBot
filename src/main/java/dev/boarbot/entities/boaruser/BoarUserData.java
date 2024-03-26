package dev.boarbot.entities.boaruser;

import dev.boarbot.entities.boaruser.collectibles.CollectedItems;
import dev.boarbot.entities.boaruser.stats.UserStats;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoarUserData {
    private CollectedItems itemCollection = new CollectedItems();
    private UserStats stats = new UserStats();
}
