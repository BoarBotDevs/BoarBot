package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.modals.ModalHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ModalListener extends ListenerAdapter implements Runnable {
    private ModalInteractionEvent event = null;

    public ModalListener() {
        super();
    }

    public ModalListener(ModalInteractionEvent event) {
        this.event = event;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        new Thread(new ModalListener(event)).start();
    }

    @Override
    public void run() {
        String initInteractionID = this.event.getModalId().split(",")[0];
        ModalHandler modalHandler = BoarBotApp.getBot().getModalHandlers().get(
            initInteractionID + this.event.getUser().getId()
        );

        if (modalHandler != null) {
            modalHandler.execute(this.event);
        }
    }
}
