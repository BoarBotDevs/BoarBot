package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.util.logging.ExceptionHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SupportSubcommand extends Subcommand {
    public SupportSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.reply(STRS.getSupportStr()).setEphemeral(true)
            .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
    }
}
