package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.collection.CollectionInteractive;
import dev.boarbot.interactives.boar.collection.CollectionView;
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

        this.interaction.deferReply().complete();

        Interactive interactive = InteractiveFactory.constructCollectionInteractive(
            this.event, CollectionView.COLLECTION
        );
        interactive.execute(null);
    }
}
