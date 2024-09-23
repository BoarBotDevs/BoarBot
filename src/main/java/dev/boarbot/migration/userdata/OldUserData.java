package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OldUserData {
    @Setter private String userID = "";
    @Setter private String username;
    private ItemCollectionData itemCollection = new ItemCollectionData();
    private StatsData stats = new StatsData();
}
