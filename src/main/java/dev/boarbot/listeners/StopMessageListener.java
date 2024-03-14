package dev.boarbot.listeners;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StopMessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        boolean fromDM = event.getMessage().isFromType(ChannelType.PRIVATE);
        boolean fromAuthor = event.getMessage().getAuthor().isBot();
        boolean ignoreMsg = !fromDM || fromAuthor;

        if (ignoreMsg) return;

        if (event.getMessage().getContentDisplay().trim().equalsIgnoreCase("stop")) {
            System.out.println("DISABLE NOTIFICATIONS");
        }
    }
}
