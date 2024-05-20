package dev.boarbot.listeners;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class StopMessageListener extends ListenerAdapter implements Runnable {
    private MessageReceivedEvent event = null;

    public StopMessageListener() {
        super();
    }

    public StopMessageListener(MessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(new StopMessageListener(event)).start();
    }

    @Override
    public void run() {
        boolean fromDM = this.event.getMessage().isFromType(ChannelType.PRIVATE);
        boolean fromAuthor = this.event.getMessage().getAuthor().isBot();
        boolean ignoreMsg = !fromDM || fromAuthor;

        if (ignoreMsg) return;

        if (this.event.getMessage().getContentDisplay().trim().equalsIgnoreCase("stop")) {
            System.out.println("DISABLE NOTIFICATIONS");
        }
    }
}
