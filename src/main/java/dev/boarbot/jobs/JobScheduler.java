package dev.boarbot.jobs;

import dev.boarbot.util.logging.Log;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Set;

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

            scheduler.scheduleJob(NotificationJob.getJob(), NotificationJob.getTrigger());
            scheduler.scheduleJob(TopCacheJob.getJob(), TopCacheJob.getTrigger());
            scheduler.scheduleJob(MarketCacheJob.getJob(), MarketCacheJob.getTrigger());
            scheduler.scheduleJob(WipeJob.getJob(), WipeJob.getTrigger());
            scheduler.scheduleJob(
                BlessResetJob.getJob(), Set.of(BlessResetJob.getTrigger1(), BlessResetJob.getTrigger2()), true
            );
            scheduler.scheduleJob(CleanupJob.getJob(), CleanupJob.getTrigger());

            scheduler.scheduleJob(
                SpookMessageJob.getJob(),
                Set.of(
                    SpookMessageJob.getTrigger1(),
                    SpookMessageJob.getTrigger2(),
                    SpookMessageJob.getTrigger3(),
                    SpookMessageJob.getTrigger4(),
                    SpookMessageJob.getTrigger5()
                ),
                true
            );

            Log.debug(JobScheduler.class, "Jobs successfully scheduled");
        } catch (SchedulerException exception) {
            Log.error(JobScheduler.class, "Failed to schedule one or more jobs", exception);
        }
    }
}
