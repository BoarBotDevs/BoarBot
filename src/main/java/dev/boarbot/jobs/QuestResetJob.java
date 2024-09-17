package dev.boarbot.jobs;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.QuestDataUtil;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import org.quartz.*;

import java.sql.Connection;
import java.sql.SQLException;
public class QuestResetJob implements Job {
    @Getter
    private final static JobDetail job = JobBuilder.newJob(PowerupEventJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 59 23 ? * 6")).build();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try (Connection connection = DataUtil.getConnection()) {
            QuestDataUtil.updateQuests(connection);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to get set new quests", exception);
        } catch (RuntimeException exception) {
            Log.error(this.getClass(), "A runtime exception occurred when updating quests", exception);
        }
    }
}
