package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BadgeData {
    private boolean possession = false;
    private long curObtained = 0;
}
