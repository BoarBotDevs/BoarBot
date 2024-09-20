package dev.boarbot.commands.boarmanage;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boarmanage.SetupInteractive;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SetupSubcommand extends Subcommand {
    public SetupSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        this.interaction.deferReply().setEphemeral(true).queue(null, e -> Log.warn(
            this.user, this.getClass(), "Discord exception thrown", e
        ));

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, SetupInteractive.class);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent SetupInteractive");
    }
}
