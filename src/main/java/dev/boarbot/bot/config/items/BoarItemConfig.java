package dev.boarbot.bot.config.items;

import dev.boarbot.BoarBotApp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoarItemConfig extends BaseItemConfig {
    private String file = "";
    private String staticFile;
    private String description = "";
    private String classification = BoarBotApp.getBot().getConfig().getStringConfig().getCompNoSpecies();
    private String update = BoarBotApp.getBot().getConfig().getStringConfig().getCompDefaultUpdate();
    private boolean isSB = false;
    private boolean blacklisted = false;
    private boolean secret = false;
}
