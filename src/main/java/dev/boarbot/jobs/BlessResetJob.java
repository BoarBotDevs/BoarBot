package dev.boarbot.jobs;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.UserDataUtil;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import org.quartz.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class BlessResetJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(BlessResetJob.class).build();

    @Getter private final static Trigger trigger1 = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 55 23 30 11 ?").inTimeZone(TimeZone.getTimeZone("UTC")))
        .withIdentity("trigger1")
        .build();
    @Getter private final static Trigger trigger2 = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 55 23 31 12 ?").inTimeZone(TimeZone.getTimeZone("UTC")))
        .withIdentity("trigger2")
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try (Connection connection = DataUtil.getConnection()) {
            UserDataUtil.resetOtherBless(connection);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to reset blessings", exception);
        }
    }
}
