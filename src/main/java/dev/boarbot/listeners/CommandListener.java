package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.commands.Subcommand;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;

@Log4j2
public class CommandListener extends ListenerAdapter implements Runnable {
    private final Map<String, Constructor<? extends Subcommand>> subcommands = BoarBotApp.getBot().getSubcommands();
    private SlashCommandInteractionEvent event = null;

    public CommandListener() {
        super();
    }

    public CommandListener(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        new Thread(new CommandListener(event)).start();
    }

    @Override
    public void run() {
        log.info(
            "[%s] (%s) ran '/%s %s'".formatted(
                this.event.getUser().getName(),
                this.event.getUser().getId(),
                this.event.getName(),
                this.event.getSubcommandName()
            )
        );

        try {
            Subcommand subcommand = subcommands.get(
                this.event.getName() + this.event.getSubcommandName()
            ).newInstance(this.event);
            subcommand.execute();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
