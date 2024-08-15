package dev.boarbot.interactives;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.util.generators.ItemImageGenerator;
import dev.boarbot.util.generators.ItemImageGrouper;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import lombok.extern.slf4j.Slf4j;
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
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ItemInteractive extends Interactive {
    private int page = 0;
    private final List<ItemImageGenerator> itemGens;
    private List<SelectOption> selectableBoars;
    private ActionRow[] curComponents = new ActionRow[0];

    private final List<String> boarIDs;
    private final List<Integer> boarEditions;

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getDaily();

    public ItemInteractive(
        Interaction interaction,
        List<ItemImageGenerator> itemGens,
        List<String> boarIDs,
        List<Integer> boarEditions
    ) {
        super(interaction);
        this.itemGens = itemGens;
        this.boarIDs = boarIDs;
        this.boarEditions = boarEditions;

        this.makeSelectOptions(boarIDs, boarEditions);
    }

    private void makeSelectOptions(List<String> boarIDs, List<Integer> boarEditions) {
        Map<String, RarityConfig> rarityConfigs = this.config.getRarityConfigs();
        List<SelectOption> selectableBoars = new ArrayList<>();

        for (int i=0; i<boarIDs.size(); i++) {
            RarityConfig rarityConfig = rarityConfigs.get(this.itemGens.get(i).getColorKey());

            String description = rarityConfig.name + " Boar";

            if (this.itemGens.get(i).getBucks() > 0) {
                description += " (+$%,d)".formatted(this.itemGens.get(i).getBucks());
            }

            boolean isSpecial = this.itemGens.get(i).getColorKey().equals("special");

            if (isSpecial) {
                description += " (Edition #%,d)".formatted(boarEditions.get(i));
            }

            String boarName = this.config.getItemConfig().getBoars().get(boarIDs.get(i)).getName();
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
            compEvent.deferEdit().queue();

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

        try (FileUpload imageToSend = ItemImageGrouper.groupItems(this.itemGens, this.page)) {
            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(imageToSend)
                .setComponents(this.getCurComponents());

            if (this.isStopped) {
                return;
            }

            this.updateInteractive(editedMsg.build());
        } catch (Exception exception) {
            log.error("Failed to change daily boar page!", exception);
        }
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (this.curComponents.length == 0) {
            this.makeSelectOptions(this.boarIDs, this.boarEditions);
        }

        if (this.hook != null || this.msg != null) {
            this.updateComponents(this.curComponents[0]);
            return;
        }

        try (FileUpload imageToSend = ItemImageGrouper.groupItems(this.itemGens, this.page)) {
            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(imageToSend)
                .setComponents(this.getCurComponents()[0]);

            this.updateInteractive(editedMsg.build());
        } catch (Exception exception) {
            log.error("Failed to change daily boar page!", exception);
        }
    }

    @Override
    public ActionRow[] getCurComponents() {
        if (this.curComponents.length == 0) {
            this.curComponents = this.getComponents();
        }

        SelectMenu boarSelect = (SelectMenu) this.curComponents[0].getComponents().getFirst();
        Button leftFullBtn = ((Button) this.curComponents[1].getComponents().get(0)).withDisabled(false);
        Button leftBtn = ((Button) this.curComponents[1].getComponents().get(1)).withDisabled(false);
        Button rightBtn = ((Button) this.curComponents[1].getComponents().get(2)).withDisabled(false);
        Button rightFullBtn = ((Button) this.curComponents[1].getComponents().get(3)).withDisabled(false);

        for (int i=0; i<this.selectableBoars.size(); i++) {
            if (i == page) {
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

        this.curComponents[0].getComponents().set(0, boarSelect);

        if (this.page == 0) {
            leftFullBtn = leftFullBtn.asDisabled();
            leftBtn = leftBtn.asDisabled();
        }

        if (this.page == this.itemGens.size()-1) {
            rightFullBtn = rightFullBtn.asDisabled();
            rightBtn = rightBtn.asDisabled();
        }

        this.curComponents[1].getComponents().set(0, leftFullBtn);
        this.curComponents[1].getComponents().set(1, leftBtn);
        this.curComponents[1].getComponents().set(2, rightBtn);
        this.curComponents[1].getComponents().set(3, rightFullBtn);

        return this.curComponents;
    }

    private ActionRow[] getComponents() {
        List<ItemComponent> boarSelect = InteractiveUtil.makeComponents(
            this.interactionID, this.COMPONENTS.get("boarSelect")
        );
        List<ItemComponent> navRow = InteractiveUtil.makeComponents(
            this.interactionID,
            this.COMPONENTS.get("leftFullBtn"),
            this.COMPONENTS.get("leftBtn"),
            this.COMPONENTS.get("rightBtn"),
            this.COMPONENTS.get("rightFullBtn")
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
        User user,
        String title,
        InteractionHook hook,
        boolean isNewMsg
    ) {
        List<ItemImageGenerator> itemGens = ItemImageGenerator.getItemImageGenerators(
            boarIDs, bucksGotten, user, title
        );

        if (itemGens.size() > 1) {
            Interactive interactive = InteractiveFactory.constructItemInteractive(
                hook.getInteraction(), itemGens, boarIDs, editions
            );
            interactive.execute(null);
        } else {
            try (FileUpload imageToSend = ItemImageGrouper.groupItems(itemGens, 0)) {
                if (isNewMsg) {
                    MessageCreateBuilder msg = new MessageCreateBuilder()
                        .setFiles(imageToSend)
                        .setComponents();

                    hook.sendMessage(msg.build()).complete();
                } else {
                    MessageEditBuilder editedMsg = new MessageEditBuilder()
                        .setFiles(imageToSend)
                        .setComponents();

                    hook.editOriginal(editedMsg.build()).complete();
                }
            } catch (Exception exception) {
                log.error("Failed to send daily boar response!", exception);
            }
        }
    }
}
