package dev.boarbot.jobs;

import dev.boarbot.bot.CacheLoader;
import lombok.Getter;
import org.quartz.*;

import java.util.TimeZone;

public class TopCacheJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(TopCacheJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * ? * *").inTimeZone(TimeZone.getTimeZone("UTC")))
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        CacheLoader.reloadTopCache();
    }
}
