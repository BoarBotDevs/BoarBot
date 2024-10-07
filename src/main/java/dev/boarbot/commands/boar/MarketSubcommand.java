package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MarketSubcommand extends Subcommand {
    public MarketSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.deferReply(true).queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, MarketInteractive.class);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent MarketInteractive");
    }
}
