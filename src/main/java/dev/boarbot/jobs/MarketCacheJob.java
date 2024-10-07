package dev.boarbot.jobs;

import dev.boarbot.bot.CacheLoader;
import lombok.Getter;
import org.quartz.*;

public class MarketCacheJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(MarketCacheJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("*/30 * * ? * *"))
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        CacheLoader.reloadMarketCache();
    }
}
