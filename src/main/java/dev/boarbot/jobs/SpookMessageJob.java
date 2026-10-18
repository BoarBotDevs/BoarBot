package dev.boarbot.jobs;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.quartz.*;

import java.util.TimeZone;

public class SpookMessageJob implements Job, Configured {
    @Getter private final static JobDetail job = JobBuilder.newJob(SpookMessageJob.class)
        .withIdentity("spookJob").build();

    @Getter private final static Trigger trigger1 = TriggerBuilder.newTrigger()
        .withSchedule(
            CronScheduleBuilder.cronSchedule("0 0 16 31 10 ?").inTimeZone(TimeZone.getTimeZone("America/Chicago"))
        )
        .withIdentity("trigger1")
        .build();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String triggerName = context.getTrigger().getKey().getName();

        MessageChannel spookChannel = BoarBotApp.getBot().getJDA()
            .getChannelById(MessageChannel.class, CONFIG.getMainConfig().getHuntChannel());

        if (spookChannel == null) {
            Log.warn(this.getClass(), "Failed to find spook channel");
            return;
        }

        String messageStr = null;

        try {
            messageStr = STRS.getSpookMessages()[0];
        } catch (IndexOutOfBoundsException exception) {
            Log.error(this.getClass(), "Failed to find Boar-O-Ween message to send", exception);
        }

        if (messageStr == null) {
            return;
        }

        try {
            spookChannel.sendMessage(messageStr).queue(
                null, e -> Log.error(this.getClass(), "Failed to send Boar-O-Ween message", e)
            );
        } catch (InsufficientPermissionException exception) {
            Log.error(this.getClass(), "Failed to send Boar-O-Ween message", exception);
        }
    }
}
