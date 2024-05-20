package dev.boarbot.listeners;

import dev.boarbot.BoarBotApp;
import dev.boarbot.interactives.Interactive;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Log4j2
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
        String initInteractionID = this.event.getComponentId().split(",")[0];
        Interactive interactive = BoarBotApp.getBot().getInteractives().get(
            initInteractionID + this.event.getUser().getId()
        );
        long startTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();

        System.out.println(initInteractionID);

        if (interactive != null) {
            interactive.attemptExecute(this.event, startTime);
        }
    }
}