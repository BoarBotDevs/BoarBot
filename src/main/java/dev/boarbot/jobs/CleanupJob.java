package dev.boarbot.jobs;

import dev.boarbot.util.graphics.GraphicsUtil;
import dev.boarbot.util.interaction.InteractionUtil;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import org.quartz.*;

public class CleanupJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(CleanupJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("*/10 * * ? * *")).build();

    private final static int COOLDOWN_MAX = 5000;
    private final static int CACHE_MAX = 60000;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        InteractionUtil.usersOnCooldown.entrySet()
            .removeIf(cooldownEntry -> cooldownEntry.getValue() < TimeUtil.getCurMilli() - COOLDOWN_MAX);

        GraphicsUtil.imageCache.entrySet()
            .removeIf(cacheEntry -> cacheEntry.getValue().lastAccessTimestamp() < TimeUtil.getCurMilli() - CACHE_MAX);
    }
}
