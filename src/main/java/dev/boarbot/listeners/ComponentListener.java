package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ComponentListener extends ListenerAdapter implements Runnable {
    private GenericComponentInteractionCreateEvent event = null;

    public ComponentListener() {
        super();
    }

    public ComponentListener(GenericComponentInteractionCreateEvent event) {
        this.event = event;
    }

    @Override
    public void onGenericComponentInteractionCreate(@NotNull GenericComponentInteractionCreateEvent event) {
        new Thread(new ComponentListener(event)).start();
    }

    @Override
    public void run() {
        String[] componentID = this.event.getComponentId().split(",");
        String interactiveBaseID = componentID[0];
        Interactive interactive = BoarBotApp.getBot().getInteractives().get(
            interactiveBaseID + "," + this.event.getUser().getId()
        );
        long startTime = TimeUtil.getCurMilli();

        if (interactive == null) {
            interactive = InteractiveUtil.getEventInteractive(interactiveBaseID);
        }

        if (interactive == null) {
            interactive = InteractiveUtil.getGiftInteractive(interactiveBaseID);
        }

        if (interactive == null) {
            return;
        }

        Log.debug(
            this.event.getUser(),
            this.getClass(),
            "Pressed %s in %s".formatted(componentID[1], interactive.getClass().getSimpleName())
        );

        try {
            interactive.attemptExecute(this.event, startTime);
            Log.debug(
                this.event.getUser(),
                this.getClass(),
                "Finished processing %s in %s".formatted(componentID[1], interactive.getClass().getSimpleName())
            );
        } catch (ErrorResponseException exception) {
            Log.warn(
                this.event.getUser(),
                this.getClass(),
                "%s threw a Discord exception".formatted(interactive.getClass().getSimpleName()),
                exception
            );
        } catch (RuntimeException exception) {
            Log.error(
                this.event.getUser(),
                this.getClass(),
                "%s threw a runtime exception".formatted(interactive.getClass().getSimpleName()),
                exception
            );
        }
    }
}