package dev.boarbot.util.interactive;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.interactives.event.EventInteractive;
import dev.boarbot.interactives.gift.BoarGiftInteractive;
import dev.boarbot.interactives.Interactive;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import net.dv8tion.jda.internal.interactions.component.EntitySelectMenuImpl;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;
import net.dv8tion.jda.internal.interactions.component.TextInputImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class InteractiveUtil {
    public static List<ItemComponent> makeComponents(String id, IndivComponentConfig... components) {
        return InteractiveUtil.makeComponents(id, "", components);
    }

    public static List<ItemComponent> makeComponents(String id, List<IndivComponentConfig> components) {
        return InteractiveUtil.makeComponents(id, "", components.toArray(new IndivComponentConfig[0]));
    }

    public static List<ItemComponent> makeComponents(String id, String extra, IndivComponentConfig... components) {
        List<ItemComponent> madeComponents = new ArrayList<>();

        if (!extra.isEmpty()) {
            extra = "_" + extra;
        }

        for (IndivComponentConfig component : components) {
            String newCustomID = id + "," + component.getCustom_id() + extra;

            switch (component.getType()) {
                case 2 -> {
                    Button btn = new ButtonImpl(
                        newCustomID,
                        component.getLabel(),
                        ButtonStyle.fromKey(component.getStyle()),
                        component.getUrl(),
                        component.isDisabled(),
                        InteractiveUtil.parseEmoji(component.getEmoji())
                    );

                    madeComponents.add(btn);
                }

                case 3 -> {
                    List<SelectOption> selectOptions = new ArrayList<>();

                    if (component.getOptions() != null) {
                        for (SelectOptionConfig option : component.getOptions()) {
                            SelectOption newOption = SelectOption.of(option.getLabel(), option.getValue())
                                .withEmoji(InteractiveUtil.parseEmoji(option.getEmoji()))
                                .withDescription(option.getDescription());
                            selectOptions.add(newOption);
                        }
                    }

                    SelectMenu select = new StringSelectMenuImpl(
                        newCustomID,
                        component.getPlaceholder(),
                        component.getMin_values(),
                        component.getMax_values(),
                        component.isDisabled(),
                        selectOptions
                    );

                    madeComponents.add(select);
                }

                case 4 -> {
                    TextInput textInput = new TextInputImpl(
                        component.getCustom_id(),
                        TextInputStyle.fromKey(component.getStyle()),
                        component.getLabel(),
                        component.getMin_length(),
                        component.getMax_length(),
                        component.getRequired(),
                        component.getValue(),
                        component.getPlaceholder()
                    );

                    madeComponents.add(textInput);
                }

                case 8 -> {
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
        }

        return madeComponents;
    }

    public static Interactive getEventInteractive(String interactiveBaseID) {
        for (String key : BoarBotApp.getBot().getInteractives().keySet()) {
            Interactive interactive = BoarBotApp.getBot().getInteractives().get(key);

            if (key.startsWith(interactiveBaseID) && interactive instanceof EventInteractive) {
                return interactive;
            } else if (key.startsWith(interactiveBaseID)) {
                return null;
            }
        }

        return null;
    }

    public static Interactive getGiftInteractive(String interactiveBaseID) {
        for (String key : BoarBotApp.getBot().getInteractives().keySet()) {
            Interactive interactive = BoarBotApp.getBot().getInteractives().get(key);

            if (key.startsWith(interactiveBaseID) && interactive instanceof BoarGiftInteractive) {
                return interactive;
            } else if (key.startsWith(interactiveBaseID)) {
                return null;
            }
        }

        return null;
    }

    public static Emoji parseEmoji(String emojiStr) {
        Emoji emoji = null;

        if (emojiStr != null && emojiStr.contains("<:")) {
            String emojiName = emojiStr.substring(2, emojiStr.indexOf(":", 2));
            long emojiID = Long.parseLong(emojiStr.substring(
                emojiStr.indexOf(":", 2) + 1, emojiStr.indexOf(">")
            ));

            emoji = Emoji.fromCustom(emojiName, emojiID, false);
        } else if (emojiStr != null) {
            emoji = Emoji.fromUnicode(emojiStr);
        }

        return emoji;
    }
}
