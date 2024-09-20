package dev.boarbot.bot;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.commands.CommandConfig;
import dev.boarbot.bot.config.commands.SubcommandConfig;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class CommandLoader implements Configured {
    public static void deployCommands() {
        Log.info(CommandLoader.class, "Deploying commands...");

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
            devGuild.updateCommands().addCommands(guildCommands).queue(null, e -> Log.warn(
                CommandLoader.class, "Discord exception thrown", e
            ));
        } else {
            Log.warn(CommandLoader.class, "Unable to find development guild. Could not deploy developer commands!");
        }

        BoarBotApp.getBot().getJDA().updateCommands().addCommands(globalCommands).queue(null, e -> Log.warn(
            CommandLoader.class, "Discord exception thrown", e
        ));
        Log.info(CommandLoader.class, "Commands successfully deployed");
    }

    public static void registerSubcommands() {
        Map<String, CommandConfig> commandData = CONFIG.getCommandConfig();

        for (CommandConfig commandVal : commandData.values()) {
            Map<String, SubcommandConfig> subcommandData = commandVal.getSubcommands();

            for (SubcommandConfig subcommandVal : subcommandData.values()) {
                String subcommandName = "/%s %s".formatted(commandVal.getName(), subcommandVal.getName());
                Log.debug(CommandLoader.class, "Registering subcommand %s...".formatted(subcommandName));

                try {
                    Class<? extends Subcommand> subcommandClass = Class.forName(subcommandVal.getLocation())
                        .asSubclass(Subcommand.class);
                    Constructor<? extends Subcommand> subcommandConstructor = subcommandClass
                        .getDeclaredConstructor(SlashCommandInteractionEvent.class);

                    BoarBotApp.getBot().getSubcommands()
                        .put(commandVal.getName() + subcommandVal.getName(), subcommandConstructor);
                } catch (ClassNotFoundException exception) {
                    Log.error(
                        CommandLoader.class, "Invalid class location for %s".formatted(subcommandName), exception
                    );
                    System.exit(-1);
                } catch (NoSuchMethodException exception) {
                    Log.error(
                        CommandLoader.class, "%s does not have a valid constructor".formatted(subcommandName), exception
                    );
                    System.exit(-1);
                }

                Log.debug(
                    CommandLoader.class,
                    "Subcommand /%s %s registered".formatted(commandVal.getName(), subcommandVal.getName())
                );
            }
        }
    }
}
