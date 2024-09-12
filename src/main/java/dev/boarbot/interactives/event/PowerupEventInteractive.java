package dev.boarbot.interactives.event;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

public class PowerupEventInteractive extends EventInteractive {
    public PowerupEventInteractive(TextChannel channel) {
        super(channel);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {

    }

    @Override
    public ActionRow[] getCurComponents() {
        return new ActionRow[0];
    }
}
