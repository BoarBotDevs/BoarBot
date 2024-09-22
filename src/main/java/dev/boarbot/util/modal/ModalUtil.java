package dev.boarbot.util.modal;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.util.interactive.InteractiveUtil;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;

import java.util.ArrayList;
import java.util.List;

public final class ModalUtil {
    public static List<LayoutComponent> makeModalComponents(List<IndivComponentConfig> components) {
        List<ItemComponent> componentsMade = InteractiveUtil.makeComponents("", components);

        List<LayoutComponent> layoutComponents = new ArrayList<>();
        for (ItemComponent component : componentsMade) {
            layoutComponents.add(ActionRow.of(component));
        }

        return layoutComponents;
    }

    public static String findDuplicateModalHandler(String userID) {
        for (String key : BoarBotApp.getBot().getModalHandlers().keySet()) {
            boolean isUserModalHandler = key.endsWith(userID);

            if (isUserModalHandler) {
                return key;
            }
        }

        return null;
    }

    public static String makeModalID(String modalID, GenericComponentInteractionCreateEvent compEvent) {
        return compEvent.getInteraction().getId() + "," + compEvent.getInteraction().getUser().getId() + "," + modalID;
    }
}
