package dev.boarbot.listeners;

import dev.boarbot.api.util.Configured;
import dev.boarbot.jobs.JobScheduler;
import dev.boarbot.util.logging.DiscordLog;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ReadyListener extends ListenerAdapter implements Configured {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        TextChannel logChannel = event.getJDA().getTextChannelById(CONFIG.getMainConfig().getLogChannel());
        ForumChannel reportChannel = event.getJDA().getForumChannelById(CONFIG.getMainConfig().getReportsChannel());
        TextChannel pingChannel = event.getJDA().getTextChannelById(CONFIG.getMainConfig().getPingChannel());

        if (logChannel == null) {
            log.warn("Invalid log channel ID. Channel logs are disabled!");
        }

        if (reportChannel == null) {
            log.warn("Invalid report channel ID. Player reporting is disabled!");
        }

        if (pingChannel == null) {
            log.warn("Invalid ping channel ID. The legacy channel pinging system is disabled!");
        }

        DiscordLog.forceLog(this.getClass(), "Bot is online!");

        JobScheduler.scheduleJobs();
    }
}
