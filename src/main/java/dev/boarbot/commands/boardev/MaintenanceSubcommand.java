package dev.boarbot.commands.boardev;

import dev.boarbot.bot.ConfigUpdater;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;
import java.util.Objects;

public class MaintenanceSubcommand extends Subcommand {
    public MaintenanceSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        try {
            ConfigUpdater.setMaintenance(Objects.requireNonNull(this.event.getOption("value")).getAsBoolean());
            this.interaction.reply("Maintenance status: `" + CONFIG.getMainConfig().isMaintenanceMode() + "`")
                .setEphemeral(true).queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
        } catch (IOException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to update maintenance status in file", exception);
        }
    }
}
