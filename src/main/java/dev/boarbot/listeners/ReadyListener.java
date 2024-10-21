package dev.boarbot.listeners;

import dev.boarbot.api.util.Configured;
import dev.boarbot.jobs.JobScheduler;
import dev.boarbot.util.logging.Log;
import lombok.Getter;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReadyListener extends ListenerAdapter implements Configured {
    @Getter private static boolean done = false;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        ReadyListener.done = true;

        TextChannel logChannel = event.getJDA().getTextChannelById(CONFIG.getMainConfig().getLogChannel());
        ForumChannel reportChannel = event.getJDA().getForumChannelById(CONFIG.getMainConfig().getReportsChannel());
        TextChannel pingChannel = event.getJDA().getTextChannelById(CONFIG.getMainConfig().getPingChannel());
        NewsChannel spookChannel = event.getJDA().getNewsChannelById(CONFIG.getMainConfig().getSpookChannel());

        if (logChannel == null) {
            Log.warn(this.getClass(), "Invalid log channel ID. Channel logs are disabled!");
        }

        if (reportChannel == null) {
            Log.warn(this.getClass(), "Invalid report channel ID. Player reporting is disabled!");
        }

        if (pingChannel == null) {
            Log.warn(this.getClass(), "Invalid ping channel ID. The legacy channel pinging system is disabled!");
        }

        if (spookChannel == null) {
            Log.warn(this.getClass(), "Invalid spook channel ID. Boar-O-Ween messages are disabled!");
        }

        Log.info(this.getClass(), "Bot is online!", true);

        JobScheduler.scheduleJobs();
    }
}
