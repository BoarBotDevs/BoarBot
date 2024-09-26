package dev.boarbot.commands.boarmanage;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boarmanage.SetupInteractive;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SetupSubcommand extends Subcommand {
    public SetupSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        this.interaction.deferReply(true).queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, SetupInteractive.class);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent SetupInteractive");
    }
}
