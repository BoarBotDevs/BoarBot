package dev.boarbot.interactives;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.*;

public abstract class Interactive implements Configured {
    protected static final ConcurrentMap<String, Interactive> interactives = BoarBotApp.getBot().getInteractives();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;

    @Getter protected final String interactiveID;
    @Getter protected final String guildID;

    protected long waitTime;
    protected long curStopTime;
    protected final long hardStopTime;
    protected long lastEndTime = 0;
    protected boolean isStopped = false;

    protected Interactive(String interactiveID, String guildID) {
        this(interactiveID, guildID, NUMS.getInteractiveIdle(), NUMS.getInteractiveHardStop());
    }

    protected Interactive(String interactiveID, String guildID, long waitTime, long hardStop) {
        this(interactiveID, guildID, waitTime, hardStop, true);
    }

    protected Interactive(String interactiveID, String guildID, long waitTime, long hardStop, boolean checkDupe) {
        this.interactiveID = interactiveID;
        this.guildID = guildID;
        this.waitTime = waitTime;
        this.curStopTime = TimeUtil.getCurMilli() + waitTime;
        this.hardStopTime = TimeUtil.getCurMilli() + hardStop;

        if (checkDupe) {
            String duplicateInteractiveKey = this.findDuplicateKey();
            if (duplicateInteractiveKey != null) {
                interactives.get(duplicateInteractiveKey).stop(StopType.EXPIRED);
            }
        }

        interactives.put(interactiveID, this);
        this.future = this.scheduler.schedule(this::tryStop, waitTime, TimeUnit.MILLISECONDS);
    }

    protected String findDuplicateKey() {
        for (String key : interactives.keySet()) {
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
            compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(compEvent, this, e));
            Log.debug(compEvent.getUser(), this.getClass(), "Clicked too fast!");
            return;
        }

        this.curStopTime = TimeUtil.getCurMilli() + this.waitTime;
        this.execute(compEvent);
        this.lastEndTime = TimeUtil.getCurMilli() + this.waitTime;
    }

    public abstract void execute(GenericComponentInteractionCreateEvent compEvent);
    public abstract ActionRow[] getCurComponents();

    public abstract void updateInteractive(boolean stopping, MessageEditData editedMsg);
    public abstract void updateComponents(boolean stopping, ActionRow... rows);
    public abstract void deleteInteractive(boolean stopping);

    private void tryStop() {
        try {
            long curTime = TimeUtil.getCurMilli();

            if ((this.curStopTime <= curTime || hardStopTime <= curTime) && !this.isStopped) {
                this.stop(StopType.EXPIRED);
            } else if (!this.isStopped) {
                long newWaitTime = Math.min(this.curStopTime - curTime, hardStopTime - curTime);
                this.future = this.scheduler.schedule(this::tryStop, newWaitTime, TimeUnit.MILLISECONDS);
            }
        } catch (RuntimeException exception) {
            Log.error(this.getClass(), "Failed to stop interactive", exception);
        }
    }

    public abstract void stop(StopType type);

    public boolean isStopped() {
        return this.isStopped;
    }

    public Interactive removeInteractive() {
        this.future.cancel(false);
        this.scheduler.shutdown();
        return interactives.remove(this.interactiveID);
    }

    public void shutdownScheduler() {
        this.scheduler.shutdown();
    }
}
