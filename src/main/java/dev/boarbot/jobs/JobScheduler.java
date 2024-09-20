package dev.boarbot.jobs;

import dev.boarbot.util.logging.Log;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.SQLException;

public class JobScheduler {
    public static void scheduleJobs() {
        try {
            Log.debug(JobScheduler.class, "Scheduling jobs...");

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            scheduler.scheduleJob(PowerupEventJob.getJob(), PowerupEventJob.getTrigger());
            scheduler.scheduleJob(QuestResetJob.getJob(), QuestResetJob.getTrigger());
            scheduler.scheduleJob(LogJob.getJob(), LogJob.getTrigger());

            try {
                NotificationEventJob.cacheNotifUsers();
            } catch (SQLException exception) {
                Log.error(JobScheduler.class, "Failed to cache users with notifications on", exception);
                System.exit(-1);
            }

            scheduler.scheduleJob(NotificationEventJob.getJob(), NotificationEventJob.getTrigger());

            Log.debug(JobScheduler.class, "Jobs successfully scheduled");
        } catch (SchedulerException exception) {
            Log.error(JobScheduler.class, "Failed to schedule one or more jobs", exception);
        }

        // TODO
        // This will schedule the following:
        // Removing wiped users
    }
}
