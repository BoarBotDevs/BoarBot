package dev.boarbot.migration.guilddata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OldGuildData {
    private boolean isSBServer = false;
    private String[] channels = {};
    @Setter private String guildID;
}
