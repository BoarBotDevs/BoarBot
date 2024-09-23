package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StatsData {
    private GeneralStatsData general = new GeneralStatsData();
    private PowerupStatsData powerups = new PowerupStatsData();
    private QuestsStatsData quests = new QuestsStatsData();
}
