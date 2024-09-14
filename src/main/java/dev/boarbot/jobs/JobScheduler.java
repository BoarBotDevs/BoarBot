package dev.boarbot.jobs;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.QuestDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class JobScheduler {
    public static void scheduleJobs() {
        try {
            log.info("Scheduling jobs...");

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            scheduler.scheduleJob(PowerupEventJob.getJob(), PowerupEventJob.getTrigger());

            try (Connection connection = DataUtil.getConnection()) {
                if (QuestDataUtil.needNewQuests(connection)) {
                    QuestDataUtil.updateQuests(connection);
                }
            }

            scheduler.scheduleJob(QuestResetJob.getJob(), QuestResetJob.getTrigger());

            log.info("Jobs successfully scheduled");
        } catch (SQLException exception) {
            log.error("Failed to get quest status", exception);
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
