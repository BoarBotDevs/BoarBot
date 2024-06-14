package dev.boarbot.modals;

import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public class PageInputModalHandler extends ModalHandler {
    private final ModalInteractive receiver;

    public PageInputModalHandler(GenericComponentInteractionCreateEvent compEvent, ModalInteractive receiver) {
        super(compEvent);
        this.receiver = receiver;
    }

    @Override
    public void execute(ModalInteractionEvent modalEvent) {
        this.receiver.attemptExecute(null, modalEvent, TimeUtil.getCurMilli());
        this.stop();
    }
}
