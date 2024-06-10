package dev.boarbot.bot.config.modals;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ModalConfig {
    private String id = "";
    private String title = "";
    private List<IndivComponentConfig> components = new ArrayList<>();
}
