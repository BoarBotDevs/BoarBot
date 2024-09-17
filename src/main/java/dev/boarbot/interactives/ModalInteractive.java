package dev.boarbot.interactives;

import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.Interaction;

@Setter
@Getter
public abstract class ModalInteractive extends UserInteractive {
    protected ModalHandler modalHandler = null;

    protected ModalInteractive(Interaction interaction) {
        super(interaction);
    }

    public synchronized void attemptExecute(
        GenericComponentInteractionCreateEvent compEvent, ModalInteractionEvent modalEvent, long startTime
    ) {
        if (startTime < this.lastEndTime) {
            Log.debug(compEvent.getUser(), this.getClass(), "Clicked too fast!");
            return;
        }

        this.curStopTime = TimeUtil.getCurMilli() + NUMS.getInteractiveIdle();

        if (compEvent != null) {
            this.execute(compEvent);
        } else {
            this.modalExecute(modalEvent);
        }

        this.lastEndTime = TimeUtil.getCurMilli();
    }

    public abstract void modalExecute(ModalInteractionEvent modalEvent);
}
