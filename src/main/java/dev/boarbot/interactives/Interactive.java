package dev.boarbot.interactives;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class Interactive {
    protected static final BotConfig config = BoarBotApp.getBot().getConfig();
    protected static final StringConfig strConfig = config.getStringConfig();
    protected static final NumberConfig nums = config.getNumberConfig();

    protected static final Map<String, Interactive> interactives = BoarBotApp.getBot().getInteractives();

    @Getter protected final String interactiveID;
    @Getter protected final String guildID;

    protected long waitTime;
    protected long curStopTime;
    protected final long hardStopTime;
    protected long lastEndTime = 0;
    protected boolean isStopped = false;

    protected Interactive(String interactiveID, String guildID) {
        this(interactiveID, guildID, nums.getInteractiveIdle(), nums.getInteractiveHardStop());
    }

    protected Interactive(String interactiveID, String guildID, long waitTime, long hardStop) {
        this.interactiveID = interactiveID;
        this.guildID = guildID;
        this.waitTime = waitTime;
        this.curStopTime = TimeUtil.getCurMilli() + waitTime;
        this.hardStopTime = TimeUtil.getCurMilli() + hardStop;

        String duplicateInteractiveKey = this.findDuplicateKey();

        if (duplicateInteractiveKey != null) {
            try {
                interactives.get(duplicateInteractiveKey).stop(StopType.EXPIRED);
            } catch (Exception exception) {
                log.error("Something went wrong when terminating interactive!", exception);
                return;
            }
        }

        interactives.put(interactiveID, this);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> this.tryStop(waitTime));
        executor.shutdown();
    }

    protected String findDuplicateKey() {
        for (String key : BoarBotApp.getBot().getInteractives().keySet()) {
            boolean isSameUser = this instanceof UserInteractive && key.endsWith(this.interactiveID.split(",")[1]);
            boolean isSameType = this.getClass().equals(interactives.get(key).getClass());

            if (isSameUser && isSameType) {
                return key;
            }
        }

        return null;
    }

    public synchronized void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        if (startTime < this.lastEndTime) {
            return;
        }

        this.curStopTime = TimeUtil.getCurMilli() + this.waitTime;
        this.execute(compEvent);
        this.lastEndTime = TimeUtil.getCurMilli();
    }

    public abstract void execute(GenericComponentInteractionCreateEvent compEvent);
    public abstract ActionRow[] getCurComponents();

    public abstract Message updateInteractive(MessageEditData editedMsg);
    public abstract Message updateComponents(ActionRow... rows);
    public abstract void deleteInteractive();

    private void tryStop(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException exception) {
            try {
                if (!this.isStopped) {
                    this.stop(StopType.EXPIRED);
                }
            } catch (Exception ignored) {}
        }

        long curTime = TimeUtil.getCurMilli();

        if (this.curStopTime <= curTime || hardStopTime <= curTime) {
            try {
                if (!this.isStopped) {
                    this.stop(StopType.EXPIRED);
                }
            } catch (Exception ignored) {}
        } else {
            long newWaitTime = Math.min(this.curStopTime - curTime, hardStopTime - curTime);
            this.tryStop(newWaitTime);
        }
    }

    public abstract void stop(StopType type) throws IOException, InterruptedException;

    public boolean isStopped() {
        return this.isStopped;
    }

    public Interactive removeInteractive() {
        return interactives.remove(this.interactiveID);
    }
}
