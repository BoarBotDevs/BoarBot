package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SupportSubcommand extends Subcommand {
    public SupportSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {}
}
