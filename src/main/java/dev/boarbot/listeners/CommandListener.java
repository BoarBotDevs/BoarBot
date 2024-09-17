package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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
        String commandStr = "/%s %s".formatted(this.event.getName(), this.event.getSubcommandName());
        Log.debug(this.event.getUser(), this.getClass(), "Running %s".formatted(commandStr));

        if (!this.event.isFromGuild()) {
            return;
        }

        try {
            Subcommand subcommand = subcommands.get(this.event.getName() + this.event.getSubcommandName())
                .newInstance(this.event);
            subcommand.execute();
            Log.debug(this.event.getUser(), this.getClass(), "Finished processing %s".formatted(commandStr));
        } catch (InstantiationException exception) {
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's class is an abstract class".formatted(commandStr),
                exception
            );
        } catch (IllegalAccessException exception) {
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's constructor is not public".formatted(commandStr),
                exception
            );
        } catch (InvocationTargetException exception) {
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's constructor threw exception".formatted(commandStr),
                exception
            );
        } catch (ErrorResponseException exception) {
            Log.warn(
                this.event.getUser(),
                this.getClass(),
                "%s's execute method threw a Discord exception".formatted(commandStr),
                exception
            );
        } catch (RuntimeException exception) {
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's execute method threw a runtime exception".formatted(commandStr),
                exception
            );
        }
    }
}
