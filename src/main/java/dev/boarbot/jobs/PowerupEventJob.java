package dev.boarbot.jobs;

import dev.boarbot.api.util.Configured;
import dev.boarbot.events.PowerupEventHandler;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import org.quartz.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PowerupEventJob implements Job, Configured {
    @Getter private final static JobDetail job = JobBuilder.newJob(PowerupEventJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule(
            "0 %d */%d ? * *".formatted(30 - NUMS.getPowPlusMinusMins(), NUMS.getPowIntervalHours())
        )).build();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (CONFIG.getMainConfig().isMaintenanceMode()) {
            return;
        }

        int delay = (int) (Math.random() * NUMS.getPowPlusMinusMins() * 2 * 60000);
        this.scheduler.schedule(this::sendEvent, delay, TimeUnit.MILLISECONDS);
    }

    private void sendEvent() {
        try {
            PowerupEventHandler eventHandler = new PowerupEventHandler();
            eventHandler.sendEvent();
        } catch (RuntimeException exception) {
            Log.error(this.getClass(), "A runtime exception occurred with powerup event", exception);
        }

        this.scheduler.shutdown();
    }
}
