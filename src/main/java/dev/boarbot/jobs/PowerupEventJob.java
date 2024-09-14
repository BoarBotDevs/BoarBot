package dev.boarbot.jobs;

import dev.boarbot.api.util.Configured;
import dev.boarbot.events.PowerupEventHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

@Slf4j
public class PowerupEventJob implements Job, Configured {
    @Getter private final static JobDetail job = JobBuilder.newJob(PowerupEventJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule(
            "0 %d */%d ? * *".formatted(30 - NUMS.getPowPlusMinusMins(), NUMS.getPowIntervalHours())
        )).build();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            int delay = (int) (Math.random() * NUMS.getPowPlusMinusMins() * 2 * 60000);
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}

        PowerupEventHandler eventHandler = new PowerupEventHandler();
        eventHandler.sendEvent();
    }
}
