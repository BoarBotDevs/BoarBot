package dev.boarbot.jobs;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import org.quartz.*;

public class LogJob implements Job {
    @Getter private final static JobDetail job = JobBuilder.newJob(LogJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * ? * *")).build();
    private final static Bot bot = BoarBotApp.getBot();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String logString = "Interactives: %,d | ModalHandlers: %,d | Images: %,d | Bytearrays: %,d | " +
            "BoarUsers: %,d | Memory Used: %,dMB/%,dMB | Threads: %,d";

        Log.debug(
            LogJob.class,
            logString.formatted(
                bot.getInteractives().size(),
                bot.getModalHandlers().size(),
                bot.getImageCacheMap().size(),
                bot.getByteCacheMap().size(),
                BoarUserFactory.getNumBoarUsers(),
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
                (Runtime.getRuntime().maxMemory()) / (1024 * 1024),
                Thread.activeCount()
            )
        );
    }
}
