package dev.boarbot.commands.boar.megamenu;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CollectionSubcommand extends Subcommand {
    public CollectionSubcommand(SlashCommandInteractionEvent event) {
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

        Interactive interactive = InteractiveFactory.constructMegaMenuInteractive(this.event, MegaMenuView.COLLECTION);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent MegaMenuInteractive (Collection)");
    }
}
