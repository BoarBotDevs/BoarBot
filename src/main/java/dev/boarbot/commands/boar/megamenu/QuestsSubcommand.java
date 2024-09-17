package dev.boarbot.commands.boar.megamenu;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class QuestsSubcommand extends Subcommand {
    public QuestsSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.deferReply().complete();

        Log.debug(this.user, this.getClass(), "Sending MegaMenuInteractive (Quests)");
        Interactive interactive = InteractiveFactory.constructMegaMenuInteractive(this.event, MegaMenuView.QUESTS);
        interactive.execute(null);
    }
}
