package dev.boarbot.util.data.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuildData {
    private boolean fullySetup = false;
    private boolean isSB = false;
    private String[] channels = {};
}
