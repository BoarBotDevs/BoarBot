package dev.boarbot.commands.boar.megamenu;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.SQLException;

@Log4j2
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

        try {
            Interactive interactive = InteractiveFactory.constructMegaMenuInteractive(
                this.event, MegaMenuView.COLLECTION
            );
            interactive.execute(null);
        } catch (SQLException exception) {
            log.error("Failed to find user data.", exception);
        }
    }
}
