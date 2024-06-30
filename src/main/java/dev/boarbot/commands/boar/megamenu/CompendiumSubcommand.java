package dev.boarbot.commands.boar.megamenu;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.SQLException;

@Slf4j
public class CompendiumSubcommand extends Subcommand {
    public CompendiumSubcommand(SlashCommandInteractionEvent event) {
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
                this.event, MegaMenuView.COMPENDIUM
            );
            interactive.execute(null);
        } catch (SQLException exception) {
            log.error("Failed to find user data.", exception);
        }
    }
}
