package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.TopInteractive;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TopSubcommand extends Subcommand {
    public TopSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.deferReply().queue(null, e -> Log.warn(
            this.user, this.getClass(), "Failed to defer reply", e
        ));

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, TopInteractive.class);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent TopInteractive");
    }
}
