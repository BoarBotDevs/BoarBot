package dev.boarbot.modals;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public interface ModalInputReceiver {
    void attemptExecute(
        GenericComponentInteractionCreateEvent compEvent, ModalInteractionEvent modalEvent, long startTime
    );
}
