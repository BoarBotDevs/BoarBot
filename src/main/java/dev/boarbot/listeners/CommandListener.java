package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.commands.Subcommand;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.lang.reflect.Constructor;
import java.util.Map;

@Log4j2
public class CommandListener extends ListenerAdapter {
    private final Map<String, Constructor<? extends Subcommand>> subcommands = BoarBotApp.getBot().getSubcommands();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        log.info(
            "[%s] (%s) ran '/%s %s'".formatted(
                event.getUser().getName(), event.getUser().getId(), event.getName(), event.getSubcommandName()
            )
        );

        try {
            Subcommand subcommand = subcommands.get(event.getName() + event.getSubcommandName()).newInstance(event);
            subcommand.execute();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
