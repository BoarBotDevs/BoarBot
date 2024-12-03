package dev.boarbot.jobs;

import dev.boarbot.util.interaction.InteractionUtil;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import org.quartz.*;

public class CooldownFixJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(CooldownFixJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("*/10 * * ? * *")).build();

    private final static int COOLDOWN_SLEEP_TIME = 3000;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (InteractionUtil.usersOnCooldown.isEmpty()) {
            return;
        }

        for (String user : InteractionUtil.usersOnCooldown.keySet()) {
            if (InteractionUtil.usersOnCooldown.get(user) < TimeUtil.getCurMilli() - COOLDOWN_SLEEP_TIME) {
                InteractionUtil.usersOnCooldown.remove(user);
            }
        }
    }
}
