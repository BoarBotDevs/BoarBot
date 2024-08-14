package dev.boarbot.commands.boarmanage;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boarmanage.SetupInteractive;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Slf4j
public class SetupSubcommand extends Subcommand {
    public SetupSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        this.interaction.deferReply().setEphemeral(true).queue();

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, SetupInteractive.class);
        interactive.execute(null);
    }
}
