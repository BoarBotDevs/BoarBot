package dev.boarbot.modals;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.util.modal.ModalUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class ModalHandler {
    protected final BotConfig config = BoarBotApp.getBot().getConfig();

    ExecutorService executor = Executors.newSingleThreadExecutor();

    protected final GenericComponentInteractionCreateEvent compEvent;
    protected final ComponentInteraction interaction;
    protected final User user;

    protected ModalHandler(GenericComponentInteractionCreateEvent compEvent) {
        this.compEvent = compEvent;
        this.interaction = compEvent.getInteraction();
        this.user = compEvent.getUser();

        String duplicateModalKey = ModalUtil.findDuplicateModalHandler(this.user.getId(), this.getClass());

        if (duplicateModalKey != null) {
            try {
                BoarBotApp.getBot().getModalHandlers().get(duplicateModalKey).stop();
            } catch (Exception exception) {
                log.error("Something went wrong when terminating modal handler!", exception);
                return;
            }
        }

        BoarBotApp.getBot().getModalHandlers().put(this.interaction.getId() + this.user.getId(), this);

        this.executor.submit(() -> this.delayStop(
            this.config.getNumberConfig().getInteractiveIdle()
        ));
        this.executor.shutdown();
    }

    public abstract void execute(ModalInteractionEvent compEvent);

    private void delayStop(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException exception) {
            this.stop();
        }

        this.stop();
    }

    public void stop() {
        BoarBotApp.getBot().getModalHandlers().remove(this.interaction.getId() + this.user.getId());
    }
}
