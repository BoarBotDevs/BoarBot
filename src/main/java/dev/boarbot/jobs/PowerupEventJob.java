package dev.boarbot.jobs;

import dev.boarbot.api.util.Configured;
import dev.boarbot.events.PowerupEventHandler;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import org.quartz.*;

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
        } catch (InterruptedException exception) {
            Log.error(this.getClass(), "Powerup event thread was interrupted before it could complete", exception);
        }

        try {
            PowerupEventHandler eventHandler = new PowerupEventHandler();
            eventHandler.sendEvent();
        } catch (RuntimeException exception) {
            Log.error(this.getClass(), "A runtime exception occurred with powerup event", exception);
        }
    }
}
