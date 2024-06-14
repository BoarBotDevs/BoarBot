package dev.boarbot.interactives;

import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public abstract class ModalInteractive extends Interactive {
    protected ModalInteractive(SlashCommandInteractionEvent initEvent) {
        super(initEvent);
    }

    public void attemptExecute(
        GenericComponentInteractionCreateEvent compEvent, ModalInteractionEvent modalEvent, long startTime
    ) {
        if (startTime < this.lastEndTime) {
            return;
        }

        this.curStopTime = TimeUtil.getCurMilli() + this.config.getNumberConfig().getInteractiveIdle();

        if (compEvent != null) {
            this.execute(compEvent);
        } else {
            this.modalExecute(modalEvent);
        }

        this.lastEndTime = TimeUtil.getCurMilli();
    }

    public abstract void modalExecute(ModalInteractionEvent modalEvent);
}
