package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GeneralStatsData {
    private long lastDaily = 0;
    private int numDailies = 0;
    private int totalBoars = 0;
    private int boarScore = 0;
    private String favoriteBoar = "";
    private String lastBoar = "";
    private long firstDaily = 0;
    private int boarStreak = 0;
    private int highestStreak = 0;
    private int multiplier = 1;
    private int highestMulti = 0;
    private boolean notificationsOn = false;
    private String notificationChannel;
}
