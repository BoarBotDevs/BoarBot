package dev.boarbot.util.interactive;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.interactives.Interactive;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import net.dv8tion.jda.internal.interactions.component.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class InteractiveUtil {
    public static List<ItemComponent> makeComponents(String id, IndivComponentConfig... components) {
        return InteractiveUtil.makeComponents(id, "", components);
    }

    public static List<ItemComponent> makeComponents(String id, String extra, IndivComponentConfig... components) {
        List<ItemComponent> madeComponents = new ArrayList<>();

        if (!extra.isEmpty()) {
            extra = "_" + extra;
        }

        for (IndivComponentConfig component : components) {
            String newCustomID = id + "," + component.getCustom_id() + extra;

            if (component.getType() == 2) {
                Button btn = new ButtonImpl(
                    newCustomID,
                    component.getLabel(),
                    ButtonStyle.fromKey(component.getStyle()),
                    component.getUrl(),
                    component.isDisabled(),
                    component.getEmoji() == null ? null : Emoji.fromUnicode(component.getEmoji())
                );

                madeComponents.add(btn);
            } else if (component.getType() == 3) {
                SelectMenu select = new StringSelectMenuImpl(
                    newCustomID,
                    component.getPlaceholder(),
                    component.getMin_values(),
                    component.getMax_values(),
                    component.isDisabled(),
                    component.getOptions()
                );

                madeComponents.add(select);
            } else if (component.getType() == 8) {
                SelectMenu select = new EntitySelectMenuImpl(
                    newCustomID,
                    component.getPlaceholder(),
                    component.getMin_values(),
                    component.getMax_values(),
                    component.isDisabled(),
                    Component.Type.CHANNEL_SELECT,
                    EnumSet.of(ChannelType.TEXT),
                    new ArrayList<>()
                );

                madeComponents.add(select);
            }
        }

        return madeComponents;
    }

    public static String findDuplicateInteractive(String userID, Class<? extends Interactive> interactiveClass) {
        for (String key : BoarBotApp.getBot().getInteractives().keySet()) {
            boolean isUserInteractive = key.endsWith(userID);
            boolean isSameType = interactiveClass.equals(BoarBotApp.getBot().getInteractives().get(key).getClass());

            if (isUserInteractive && isSameType) {
                return key;
            }
        }

        return null;
    }
}
