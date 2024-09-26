package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.WipeInteractive;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SelfWipeSubcommand extends Subcommand {
    public SelfWipeSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, WipeInteractive.class);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent WipeInteractive");
    }
}
