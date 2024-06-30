package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.commands.Subcommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;

@Slf4j
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

        if (!this.event.isFromGuild()) {
            return;
        }

        try {
            Subcommand subcommand = subcommands.get(
                this.event.getName() + this.event.getSubcommandName()
            ).newInstance(this.event);
            subcommand.execute();
        } catch (Exception exception) {
            log.error(
                "Something went wrong when running '/%s %s'.".formatted(
                    this.event.getName(), this.event.getSubcommandName()
                ),
                exception
            );
        }
    }
}
