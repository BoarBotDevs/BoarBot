package dev.boarbot.commands;

import dev.boarbot.BoarBotApp;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.Map;

public class BoarCommands {
    public static void register() {
        JDA jda = BoarBotApp.getBot().getJDA();
        Map<String, Object> commandData = BoarBotApp.getBot().getConfig().getCommandConfig().boar;

        jda.updateCommands().addCommands(
            SlashCommandData.fromData((DataObject) commandData)
        ).queue();
    }
}
