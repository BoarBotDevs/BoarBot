package dev.boarbot.jobs;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.market.MarketDataUtil;
import dev.boarbot.util.data.market.MarketUpdateType;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import org.quartz.*;

import java.sql.Connection;
import java.sql.SQLException;

public class MarketAdjustJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(MarketAdjustJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("15 55 * ? * *"))
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try (Connection connection = DataUtil.getConnection()) {
            MarketDataUtil.updateMarket(MarketUpdateType.AUTO_ADJUST, connection);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to adjust market prices", exception);
        }
    }
}
