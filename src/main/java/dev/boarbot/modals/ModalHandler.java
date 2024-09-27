package dev.boarbot.modals;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import dev.boarbot.util.time.TimeUtil;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.concurrent.*;

public class ModalHandler implements Configured {
    private final ModalInteractive receiver;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledFuture<?> future;

    private final Interaction interaction;
    private final User user;

    public ModalHandler(Interaction interaction, ModalInteractive receiver) {
        this.interaction = interaction;
        this.user = interaction.getUser();

        this.receiver = receiver;

        String duplicateModalKey = ModalUtil.findDuplicateModalHandler(this.user.getId());

        if (duplicateModalKey != null) {
            BoarBotApp.getBot().getModalHandlers().get(duplicateModalKey).stop();
        }

        BoarBotApp.getBot().getModalHandlers().put(this.interaction.getId() + this.user.getId(), this);
        this.future = this.scheduler.schedule(this::delayStop, NUMS.getInteractiveIdle(), TimeUnit.MILLISECONDS);
    }

    public void execute(ModalInteractionEvent modalEvent) {
        this.receiver.attemptExecute(null, modalEvent, TimeUtil.getCurMilli());
        this.stop();
    }

    private void delayStop() {
        try {
            this.stop();
        } catch (RuntimeException exception) {
            Log.error(this.user, this.getClass(), "Failed to stop modal", exception);
        }
    }

    public void stop() {
        this.future.cancel(false);
        this.scheduler.shutdown();
        BoarBotApp.getBot().getModalHandlers().remove(this.interaction.getId() + this.user.getId());
    }

    public void shutdownScheduler() {
        this.scheduler.shutdown();
    }
}
