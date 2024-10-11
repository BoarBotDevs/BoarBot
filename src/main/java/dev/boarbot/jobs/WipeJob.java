package dev.boarbot.jobs;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.UserDataUtil;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import org.quartz.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class WipeJob implements Job {
    @Getter
    private final static JobDetail job = JobBuilder.newJob(WipeJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * ? * *").inTimeZone(TimeZone.getTimeZone("UTC")))
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try (Connection connection = DataUtil.getConnection()) {
            UserDataUtil.removeWipeUsers(connection);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to remove wipe users", exception);
        } catch (RuntimeException exception) {
            Log.error(this.getClass(), "A runtime exception occurred when removing wipe users", exception);
        }
    }
}
