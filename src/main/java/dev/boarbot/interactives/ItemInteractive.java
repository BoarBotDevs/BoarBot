package dev.boarbot.interactives;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.util.generators.ItemImageGenerator;
import dev.boarbot.util.generators.ItemImageGrouper;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemInteractive extends UserInteractive {
    private int page = 0;
    private final List<ItemImageGenerator> itemGens;
    private List<SelectOption> selectableBoars;

    private final List<String> boarIDs;
    private final List<Integer> boarEditions;

    private final MessageEditBuilder messageBuilder = new MessageEditBuilder();

    private final static Map<String, IndivComponentConfig> components = CONFIG.getComponentConfig().getDaily();

    public ItemInteractive(
        Interaction interaction,
        List<ItemImageGenerator> itemGens,
        List<String> boarIDs,
        List<Integer> boarEditions,
        boolean isMsg
    ) {
        super(interaction, isMsg, NUMS.getInteractiveIdle(), NUMS.getInteractiveHardStop(), false);
        this.itemGens = itemGens;
        this.boarIDs = boarIDs;
        this.boarEditions = boarEditions;

        this.makeSelectOptions(boarIDs, boarEditions);
    }

    private void makeSelectOptions(List<String> boarIDs, List<Integer> boarEditions) {
        if (this.boarIDs == null) {
            return;
        }

        List<SelectOption> selectableBoars = new ArrayList<>();

        for (int i=0; i<boarIDs.size(); i++) {
            RarityConfig rarityConfig = RARITIES.get(this.itemGens.get(i).getColorKey());

            String description = rarityConfig.getName() + " Boar";

            if (this.itemGens.get(i).getBucks() > 0) {
                description += " (+$%,d)".formatted(this.itemGens.get(i).getBucks());
            }

            if (rarityConfig.isShowEdition()) {
                description += " (Edition #%,d)".formatted(boarEditions.get(i));
            }

            String boarName = BOARS.get(boarIDs.get(i)).getName();
            SelectOption boarOption = SelectOption.of(boarName, Integer.toString(i))
                .withEmoji(InteractiveUtil.parseEmoji(rarityConfig.getEmoji()))
                .withDescription(description);

            selectableBoars.add(boarOption);
        }

        this.selectableBoars = selectableBoars;
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent != null) {
            compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(compEvent, this, e));

            if (!this.user.getId().equals(compEvent.getUser().getId())) {
                return;
            }

            String compID = compEvent.getComponentId().split(",")[1];

            switch (compID) {
                case "BOAR_SELECT" -> this.page = Integer.parseInt(
                    ((StringSelectInteractionEvent) compEvent).getValues().getFirst()
                );
                case "LEFT_FULL" -> this.page = 0;
                case "LEFT" -> this.page--;
                case "RIGHT" -> this.page++;
                case "RIGHT_FULL" -> this.page = this.itemGens.size()-1;
            }
        }

        try {
            FileUpload imageToSend = ItemImageGrouper.groupItems(this.itemGens, this.page);
            this.messageBuilder.setFiles(imageToSend);
        } catch (IOException | URISyntaxException exception) {
            this.stop(StopType.EXCEPTION);
            return;
        }

        if (this.itemGens.size() == 1) {
            this.stop(StopType.EXPIRED);
            return;
        }

        if (this.boarIDs != null) {
            this.messageBuilder.setComponents(this.getCurComponents());
        } else {
            this.messageBuilder.setComponents();
        }

        this.updateInteractive(false, this.messageBuilder.build());
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (type.equals(StopType.EXCEPTION)) {
            super.stop(type);
            return;
        }

        if (this.getComponents().length == 0) {
            this.makeSelectOptions(this.boarIDs, this.boarEditions);
        }

        if (this.boarIDs != null) {
            this.messageBuilder.setComponents(this.getCurComponents()[0]);
        } else {
            this.messageBuilder.setComponents();
        }

        this.updateInteractive(true, this.messageBuilder.build());
    }

    @Override
    public ActionRow[] getCurComponents() {
        ActionRow[] components = this.getComponents();

        SelectMenu boarSelect = (SelectMenu) components[0].getComponents().getFirst();
        Button leftFullBtn = ((Button) components[1].getComponents().get(0)).withDisabled(false);
        Button leftBtn = ((Button) components[1].getComponents().get(1)).withDisabled(false);
        Button rightBtn = ((Button) components[1].getComponents().get(2)).withDisabled(false);
        Button rightFullBtn = ((Button) components[1].getComponents().get(3)).withDisabled(false);

        for (int i=0; i<this.selectableBoars.size(); i++) {
            if (i == this.page) {
                this.selectableBoars.set(i, this.selectableBoars.get(i).withDefault(true));
                continue;
            }

            this.selectableBoars.set(i, this.selectableBoars.get(i).withDefault(false));
        }

        boarSelect = new StringSelectMenuImpl(
            boarSelect.getId(),
            boarSelect.getPlaceholder(),
            boarSelect.getMinValues(),
            boarSelect.getMaxValues(),
            boarSelect.isDisabled(),
            this.selectableBoars
        );

        components[0].getComponents().set(0, boarSelect);

        if (this.page == 0) {
            leftFullBtn = leftFullBtn.asDisabled();
            leftBtn = leftBtn.asDisabled();
        }

        if (this.page == this.itemGens.size()-1) {
            rightFullBtn = rightFullBtn.asDisabled();
            rightBtn = rightBtn.asDisabled();
        }

        components[1].getComponents().set(0, leftFullBtn);
        components[1].getComponents().set(1, leftBtn);
        components[1].getComponents().set(2, rightBtn);
        components[1].getComponents().set(3, rightFullBtn);

        return components;
    }

    private ActionRow[] getComponents() {
        List<ItemComponent> boarSelect = InteractiveUtil.makeComponents(
            this.interactionID, components.get("boarSelect")
        );
        List<ItemComponent> navRow = InteractiveUtil.makeComponents(
            this.interactionID,
            components.get("leftFullBtn"),
            components.get("leftBtn"),
            components.get("rightBtn"),
            components.get("rightFullBtn")
        );

        return new ActionRow[] {
            ActionRow.of(boarSelect),
            ActionRow.of(navRow)
        };
    }

    public static void sendInteractive(
        List<String> boarIDs,
        List<Integer> bucksGotten,
        List<Integer> editions,
        Set<String> firstBoarIDs,
        User giftingUser,
        User user,
        String title,
        InteractionHook hook,
        boolean isMsg
    ) {
        List<ItemImageGenerator> itemGens = ItemImageGenerator.getItemImageGenerators(
            boarIDs, bucksGotten, firstBoarIDs, user, title, giftingUser
        );

        Interactive interactive = InteractiveFactory.constructItemInteractive(
            hook.getInteraction(), itemGens, boarIDs, editions, isMsg
        );
        interactive.execute(null);
    }

    public static void sendInteractive(
        String itemName,
        String filePath,
        String colorKey,
        User giftingUser,
        User user,
        String title,
        boolean addNewTag,
        InteractionHook hook,
        boolean isMsg
    ) {
        List<ItemImageGenerator> itemGens = ItemImageGenerator.getItemImageGenerators(
            itemName, filePath, colorKey, user, title, giftingUser, addNewTag
        );

        Interactive interactive = InteractiveFactory.constructItemInteractive(
            hook.getInteraction(), itemGens, null, null, isMsg
        );
        interactive.execute(null);
    }
}
