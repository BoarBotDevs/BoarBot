package dev.boarbot.entities.boaruser.data;

import dev.boarbot.entities.boaruser.data.collectibles.CollectedItems;
import dev.boarbot.entities.boaruser.data.stats.UserStats;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoarUserData {
    private CollectedItems itemCollection = new CollectedItems();
    private UserStats stats = new UserStats();
}
