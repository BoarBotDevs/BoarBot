package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.time.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
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
        String interactiveBaseID = this.event.getComponentId().split(",")[0];
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

        if (interactive != null) {
            interactive.attemptExecute(this.event, startTime);
        }
    }
}