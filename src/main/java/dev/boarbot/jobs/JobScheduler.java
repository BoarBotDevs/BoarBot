package dev.boarbot.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

@Slf4j
public class JobScheduler {
    public static void scheduleJobs() {
        try {
            log.debug("Scheduling jobs...");

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            scheduler.scheduleJob(PowerupEventJob.getJob(), PowerupEventJob.getTrigger());
            scheduler.scheduleJob(QuestResetJob.getJob(), QuestResetJob.getTrigger());

            log.debug("Jobs successfully scheduled");
        } catch (SchedulerException exception) {
            log.error("Failed to schedule one or more jobs", exception);
        }

        // TODO
        // This will schedule the following:
        // Notifications
        // Logs
        // Removing wiped users
        // Sending updates to update channel
    }
}
