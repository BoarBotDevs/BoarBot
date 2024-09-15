package dev.boarbot.bot;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.commands.CommandConfig;
import dev.boarbot.bot.config.commands.SubcommandConfig;
import dev.boarbot.commands.Subcommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
class CommandLoader implements Configured {
    public static void deployCommands() {
        log.info("Deploying commands...");

        Map<String, CommandConfig> commandData = CONFIG.getCommandConfig();

        List<SlashCommandData> globalCommands = new ArrayList<>();
        List<SlashCommandData> guildCommands = new ArrayList<>();

        for (CommandConfig command : commandData.values()) {
            Integer defaultPerms = command.getDefault_member_permissions();

            if (defaultPerms != null && defaultPerms == 0) {
                guildCommands.add(SlashCommandData.fromData(DataObject.fromJson(command.toString())));
                continue;
            }

            globalCommands.add(SlashCommandData.fromData(DataObject.fromJson(command.toString())));
        }

        Guild devGuild = BoarBotApp.getBot().getJDA().getGuildById(CONFIG.getMainConfig().getDevGuild());

        if (devGuild != null) {
            devGuild.updateCommands().addCommands(guildCommands).complete();
        } else {
            log.warn("Unable to find development guild. Could not deploy developer commands!");
        }

        BoarBotApp.getBot().getJDA().updateCommands().addCommands(globalCommands).complete();
        log.info("Commands successfully deployed");
    }

    public static void registerSubcommands() {
        Map<String, CommandConfig> commandData = CONFIG.getCommandConfig();

        for (CommandConfig commandVal : commandData.values()) {
            Map<String, SubcommandConfig> subcommandData = commandVal.getSubcommands();

            for (SubcommandConfig subcommandVal : subcommandData.values()) {
                try {
                    Class<? extends Subcommand> subcommandClass = Class.forName(subcommandVal.getLocation())
                        .asSubclass(Subcommand.class);
                    Constructor<? extends Subcommand> subcommandConstructor = subcommandClass
                        .getDeclaredConstructor(SlashCommandInteractionEvent.class);

                    BoarBotApp.getBot().getSubcommands()
                        .put(commandVal.getName() + subcommandVal.getName(), subcommandConstructor);
                } catch (Exception exception) {
                    log.error(
                        "Failed to find constructor for '/%s %s'."
                            .formatted(commandVal.getName(), subcommandVal.getName()),
                        exception
                    );
                    System.exit(-1);
                }
            }
        }
    }
}
