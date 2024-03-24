package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class GuessSubcommand extends Subcommand {
    public GuessSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {}
}
