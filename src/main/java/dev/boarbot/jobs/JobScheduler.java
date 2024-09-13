package dev.boarbot.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

@Slf4j
public class JobScheduler {
    public static void scheduleJobs() {
        try {
            log.info("Scheduling jobs...");

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            scheduler.scheduleJob(PowerupEventJob.getJob(), PowerupEventJob.getTrigger());

            log.info("Jobs successfully scheduled");
        } catch (Exception exception) {
            log.error("Failed to schedule one or more jobs", exception);
        }

        // TODO
        // This will schedule the following:
        // Notifications
        // Powerup Events
        // Quest resets
        // Logs
        // Removing wiped users
        // Sending updates to update channel
    }
}
