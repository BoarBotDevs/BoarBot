package dev.boarbot.entities.boaruser.collectibles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectedTheme {
    private boolean selected = false;
    private long firstObtained = 0;
}
