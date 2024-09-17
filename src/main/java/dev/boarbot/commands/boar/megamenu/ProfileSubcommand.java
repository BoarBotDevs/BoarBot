package dev.boarbot.commands.boar.megamenu;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ProfileSubcommand extends Subcommand {
    public ProfileSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.deferReply().complete();

        Log.debug(this.user, this.getClass(), "Sending MegaMenuInteractive (Profile)");
        Interactive interactive = InteractiveFactory.constructMegaMenuInteractive(this.event, MegaMenuView.PROFILE);
        interactive.execute(null);
    }
}
