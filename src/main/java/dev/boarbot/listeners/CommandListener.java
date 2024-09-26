package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class CommandListener extends ListenerAdapter implements Runnable, Configured {
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
        if (!this.event.isFromGuild()) {
            return;
        }

        boolean isMaintenance = CONFIG.getMainConfig().isMaintenanceMode();
        boolean isDev = Arrays.asList(CONFIG.getMainConfig().getDevs()).contains(this.event.getUser().getId());

        if (isMaintenance && !isDev) {
            MessageCreateBuilder msg = new MessageCreateBuilder();

            try {
                msg.setFiles(new EmbedImageGenerator(
                    STRS.getMaintenance(), COLORS.get("maintenance")
                ).generate().getFileUpload());
            } catch (IOException exception) {
                Log.error(this.event.getUser(), this.getClass(), "Failed to generate maintenance embed", exception);
                msg.setContent(STRS.getMaintenance());
            }

            this.event.getInteraction().reply(msg.build()).setEphemeral(true).queue(null,
                e -> ExceptionHandler.replyHandle(this.event, this.getClass(), e)
            );

            return;
        }

        String commandStr = "/%s %s".formatted(this.event.getName(), this.event.getSubcommandName());
        Log.debug(this.event.getUser(), this.getClass(), "Running %s".formatted(commandStr));

        try {
            Subcommand subcommand = subcommands.get(this.event.getName() + this.event.getSubcommandName())
                .newInstance(this.event);
            subcommand.execute();

            Log.debug(this.event.getUser(), this.getClass(), "Finished processing %s".formatted(commandStr));

            subcommand.trySendEventDisabled();
        } catch (InstantiationException exception) {
            SpecialReply.sendErrorMessage(this.event.getInteraction(), this);
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's class is an abstract class".formatted(commandStr),
                exception
            );
        } catch (IllegalAccessException exception) {
            SpecialReply.sendErrorMessage(this.event.getInteraction(), this);
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's constructor is not public".formatted(commandStr),
                exception
            );
        } catch (InvocationTargetException exception) {
            SpecialReply.sendErrorMessage(this.event.getInteraction(), this);
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's constructor threw exception".formatted(commandStr),
                exception
            );
        } catch (RuntimeException exception) {
            SpecialReply.sendErrorMessage(this.event.getInteraction(), this);
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s's execute method threw a runtime exception".formatted(commandStr),
                exception
            );
        }
    }
}
