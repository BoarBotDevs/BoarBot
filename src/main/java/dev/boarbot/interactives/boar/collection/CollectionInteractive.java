package dev.boarbot.interactives.boar.collection;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import net.dv8tion.jda.api.events.interaction.GenericAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;
import org.apache.logging.log4j.core.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CollectionInteractive extends Interactive {
    private CollectionView curView;
    private ActionRow[] curComponents = new ActionRow[0];
    private List<SelectOption> navOptions = new ArrayList<>();

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getCollection();

    public CollectionInteractive(SlashCommandInteractionEvent initEvent, CollectionView curView) {
        super(initEvent);
        this.curView = curView;
        this.makeSelectOptions(this.COMPONENTS.get("viewSelect").getOptions());
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setContent("collection")
            .setComponents(this.getCurComponents());

        this.interaction.getHook().editOriginal(editedMsg.build()).complete();;
    }

    @Override
    public ActionRow[] getCurComponents() {
        ActionRow[] nav = this.getNav();

        ActionRow[] components = switch (this.curView) {
            case CollectionView.PROFILE -> getCollectionComponents();
            case CollectionView.COLLECTION -> getCollectionComponents();
            case CollectionView.COMPENDIUM -> getCollectionComponents();
            case CollectionView.STATS -> getCollectionComponents();
            case CollectionView.POWERUPS -> getCollectionComponents();
            case CollectionView.QUESTS -> getCollectionComponents();
        };

        this.curComponents = Stream.of(nav, components).flatMap(Stream::of).toArray(ActionRow[]::new);

        return this.curComponents;
    }

    private ActionRow[] getCollectionComponents() {
        List<ItemComponent> boarFindBtn = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("boarFindBtn")
        );

        return new ActionRow[] {
            ActionRow.of(boarFindBtn)
        };
    }

    private ActionRow[] getNav() {
        List<ItemComponent> viewSelect = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("viewSelect")
        );
        List<ItemComponent> navBtns = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("leftBtn"),
            this.COMPONENTS.get("pageBtn"),
            this.COMPONENTS.get("rightBtn"),
            this.COMPONENTS.get("refreshBtn")
        );

        for (int i=0; i<this.navOptions.size(); i++) {
            SelectOption navOption = this.navOptions.get(i);

            if (navOption.getValue().equals(this.curView.toString())) {
                this.navOptions.set(i, navOption.withDefault(true));
                continue;
            }

            this.navOptions.set(i, navOption.withDefault(false));
        }

        StringSelectMenu viewSelectMenu = (StringSelectMenu) viewSelect.getFirst();
        viewSelect.set(0, new StringSelectMenuImpl(
            viewSelectMenu.getId(),
            viewSelectMenu.getPlaceholder(),
            viewSelectMenu.getMinValues(),
            viewSelectMenu.getMaxValues(),
            viewSelectMenu.isDisabled(),
            this.navOptions
        ));

        return new ActionRow[] {
            ActionRow.of(viewSelect),
            ActionRow.of(navBtns)
        };
    }

    private void makeSelectOptions(List<SelectOptionConfig> options) {
        List<SelectOption> navOptions = new ArrayList<>();

        for (SelectOptionConfig option : options) {
            SelectOption navOption = SelectOption.of(option.getLabel(), option.getValue())
                .withEmoji(InteractiveUtil.parseEmoji(option.getEmoji()))
                .withDescription(option.getDescription());

            navOptions.add(navOption);
        }

        this.navOptions = navOptions;
    }
}
