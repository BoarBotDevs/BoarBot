package dev.boarbot.interactives;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class Interactive {
    protected final BotConfig config = BoarBotApp.getBot().getConfig();

    ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Interaction interaction;
    @Getter protected final String interactionID;
    @Getter protected final String guildID;
    @Getter protected final User user;
    protected InteractionHook hook;
    protected Message msg;

    private final boolean isMsg;

    protected long curStopTime = TimeUtil.getCurMilli() + this.config.getNumberConfig().getInteractiveIdle();
    protected long hardStopTime = TimeUtil.getCurMilli() + this.config.getNumberConfig().getInteractiveHardStop();
    protected long lastEndTime = 0;
    protected boolean isStopped = false;

    protected Interactive(Interaction interaction) {
        this(interaction, false);
    }

    protected Interactive(Interaction interaction, boolean isMsg) {
        this.interaction = interaction;
        this.interactionID = interaction.getId();
        this.guildID = interaction.getGuild().getId();
        this.user = interaction.getUser();
        this.hook = ((IDeferrableCallback) interaction).getHook();
        this.isMsg = isMsg;

        String duplicateInteractiveKey = InteractiveUtil.findDuplicateInteractive(this.user.getId(), this.getClass());

        if (duplicateInteractiveKey != null) {
            try {
                BoarBotApp.getBot().getInteractives().get(duplicateInteractiveKey).stop(StopType.EXPIRED);
            } catch (Exception exception) {
                log.error("Something went wrong when terminating interactive!", exception);
                return;
            }
        }

        BoarBotApp.getBot().getInteractives().put(this.interaction.getId() + this.user.getId(), this);

        this.executor.submit(() -> this.tryStop(
            this.config.getNumberConfig().getInteractiveIdle()
        ));
        this.executor.shutdown();
    }

    public synchronized void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        if (startTime < this.lastEndTime) {
            return;
        }

        this.curStopTime = TimeUtil.getCurMilli() + this.config.getNumberConfig().getInteractiveIdle();

        this.execute(compEvent);

        this.lastEndTime = TimeUtil.getCurMilli();
    }

    public abstract void execute(GenericComponentInteractionCreateEvent compEvent);
    public abstract ActionRow[] getCurComponents();

    public void updateInteractive(MessageEditData editedMsg) {
        if (this.msg == null && this.isMsg) {
            this.msg = ((ComponentInteraction) this.interaction).getHook().sendMessage(
                MessageCreateData.fromEditData(editedMsg)
            ).complete();
            return;
        }

        if (this.msg != null) {
            this.msg.editMessage(editedMsg).complete();
        } else {
            this.hook.editOriginal(editedMsg).complete();
        }
    }

    public void updateComponents(ActionRow... rows) {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.editMessageComponents(rows).complete();
        } else {
            this.hook.editOriginalComponents(rows).complete();
        }
    }

    public void deleteInteractiveMessage() {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.delete().complete();
        } else {
            this.hook.deleteOriginal().complete();
        }
    }

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

        if (this.curStopTime <= curTime || this.hardStopTime <= curTime) {
            try {
                if (!this.isStopped) {
                    this.stop(StopType.EXPIRED);
                }
            } catch (Exception ignored) {}
        } else {
            long newWaitTime = Math.min(this.curStopTime - curTime, this.hardStopTime - curTime);
            this.tryStop(newWaitTime);
        }
    }

    public void stop(StopType type) throws IOException, InterruptedException {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (this.hook != null) {
            this.hook.editOriginalComponents().complete();
        } else {
            this.msg.editMessageComponents().complete();
        }
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public Interactive removeInteractive() {
        return BoarBotApp.getBot().getInteractives().remove(this.interaction.getId() + this.user.getId());
    }
}
