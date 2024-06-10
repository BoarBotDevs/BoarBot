package dev.boarbot.util.modal;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.interactive.InteractiveUtil;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;

import java.util.List;

public final class ModalUtil {
    public static List<LayoutComponent> makeModalComponents(List<IndivComponentConfig> components) {
        List<ItemComponent> componentsMade = InteractiveUtil.makeComponents("", components);
        return List.of(ActionRow.of(componentsMade));
    }

    public static String findDuplicateModalHandler(String userID, Class<? extends ModalHandler> modalHandlerClass) {
        for (String key : BoarBotApp.getBot().getModalHandlers().keySet()) {
            boolean isUserModalHandler = key.endsWith(userID);
            boolean isSameType = modalHandlerClass.equals(BoarBotApp.getBot().getModalHandlers().get(key).getClass());

            if (isUserModalHandler && isSameType) {
                return key;
            }
        }

        return null;
    }

    public static String makeModalID(String modalID, GenericComponentInteractionCreateEvent compEvent) {
        return compEvent.getInteraction().getId() + "," + compEvent.getInteraction().getUser().getId() + "," + modalID;
    }
}
