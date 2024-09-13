package dev.boarbot.listeners;

import dev.boarbot.jobs.JobScheduler;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReadyListener extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JobScheduler.scheduleJobs();
    }
}
