package dev.boarbot.jobs;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.quartz.*;

import java.util.TimeZone;

public class EasterMessageJob implements Job, Configured {
    @Getter private final static JobDetail job = JobBuilder.newJob(EasterMessageJob.class).build();
    @Getter private final static Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 ? * *").inTimeZone(TimeZone.getTimeZone("UTC")))
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!TimeUtil.isFirstDayEaster()) {
            return;
        }

        MessageChannel huntChannel = BoarBotApp.getBot().getJDA()
            .getChannelById(MessageChannel.class, CONFIG.getMainConfig().getHuntChannel());

        if (huntChannel == null) {
            Log.warn(this.getClass(), "Failed to find hunt channel");
            return;
        }

        try {
            huntChannel.sendMessage(STRS.getEasterMessage()).queue(
                null, e -> Log.error(this.getClass(), "Failed to send Easter message", e)
            );
        } catch (InsufficientPermissionException exception) {
            Log.error(this.getClass(), "Failed to send Easter message", exception);
        }
    }
}
