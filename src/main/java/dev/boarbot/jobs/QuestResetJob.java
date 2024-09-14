package dev.boarbot.jobs;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.QuestDataUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
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
            log.error("Failed to get set new quests", exception);
        }
    }
}
