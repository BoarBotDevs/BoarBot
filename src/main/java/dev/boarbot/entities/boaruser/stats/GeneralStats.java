package dev.boarbot.entities.boaruser.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralStats {
    private long lastDaily = 0;
    private int numDailies = 0;
    private int totalBoars = 0;
    private long boarScore = 0;
    private String favoriteBoar = "";
    private String lastBoar = "";
    private long firstDaily = 0;
    private int boarStreak = 0;
    private int highestStreak = 0;
    private long multiplier = 1;
    private long highestMulti = 1;
    private boolean notificationsOn = false;
    private String notificationChannel = "";
    private Integer spook2Stage;
    private Integer spook3Stage;
    private Integer[] spookEditions;
    private Boolean[] truths;
}
