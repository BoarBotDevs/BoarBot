package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
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
        String[] modalID = this.event.getModalId().split(",");
        String initInteractionID = modalID[0];
        ModalHandler modalHandler = BoarBotApp.getBot().getModalHandlers().get(
            initInteractionID + this.event.getUser().getId()
        );

        if (modalHandler == null) {
            return;
        }

        Log.debug(this.event.getUser(), this.getClass(), "Submitted modal %s".formatted(modalID[1]));

        try {
            modalHandler.execute(this.event);
        } catch (ErrorResponseException exception) {
            Log.warn(
                this.event.getUser(),
                this.getClass(),
                "%s threw a Discord exception".formatted(modalHandler.getClass().getSimpleName()),
                exception
            );
        } catch (RuntimeException exception) {
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s threw a runtime exception".formatted(modalHandler.getClass().getSimpleName()),
                exception
            );
        }
    }
}
