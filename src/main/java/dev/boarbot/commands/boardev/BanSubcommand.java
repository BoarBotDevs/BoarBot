package dev.boarbot.commands.boardev;

import dev.boarbot.commands.Subcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class BanSubcommand extends Subcommand {
    public BanSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {}
}
