package dev.boarbot.jobs;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.UserDataUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import org.quartz.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class BlessResetJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(BlessResetJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 ? * *").inTimeZone(TimeZone.getTimeZone("UTC")))
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (TimeUtil.isDecember()) {
            return;
        }

        try (Connection connection = DataUtil.getConnection()) {
            UserDataUtil.resetOtherBless(connection);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to reset blessings", exception);
        }
    }
}
