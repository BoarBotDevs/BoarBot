package dev.boarbot.commands.boardev;

import dev.boarbot.bot.ConfigUpdater;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;

public class MaintenanceSubcommand extends Subcommand {
    public MaintenanceSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        try {
            ConfigUpdater.toggleMaintenance();
            this.interaction.reply("Maintenance status: `" + CONFIG.getMainConfig().isMaintenanceMode() + "`")
                .setEphemeral(true)
                .queue(null, e -> Log.warn(this.user, this.getClass(), "Discord exception thrown", e));
        } catch (IOException exception) {
            Log.error(this.user, this.getClass(), "Failed to update maintenance status in file", exception);
        }
    }
}
