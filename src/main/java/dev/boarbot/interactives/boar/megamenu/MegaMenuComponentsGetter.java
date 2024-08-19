package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.util.interactive.InteractiveUtil;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.util.*;

class MegaMenuComponentsGetter {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    private final MegaMenuInteractive interactive;

    private List<SelectOption> navOptions = new ArrayList<>();
    private List<SelectOption> filterOptions = new ArrayList<>();
    private List<SelectOption> sortOptions = new ArrayList<>();
    private List<SelectOption> interactOptions = new ArrayList<>();

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getMegaMenu();

    public MegaMenuComponentsGetter(MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.makeSelectOptions(this.COMPONENTS.get("viewSelect"));
    }

    public ActionRow[] getComponents() {
        return switch (this.interactive.getCurView()) {
            case MegaMenuView.PROFILE -> getProfileComponents();
            case MegaMenuView.COLLECTION -> getCompendiumCollectionComponents();
            case MegaMenuView.COMPENDIUM -> getCompendiumCollectionComponents(true);
            case MegaMenuView.STATS -> getCompendiumCollectionComponents();
            case MegaMenuView.POWERUPS -> getCompendiumCollectionComponents();
            case MegaMenuView.QUESTS -> getCompendiumCollectionComponents();
            case MegaMenuView.BADGES -> getCompendiumCollectionComponents();
        };
    }

    private ActionRow[] getProfileComponents() {
        ActionRow[] nav = getNav();

        Button leftBtn = ((Button) nav[1].getComponents().getFirst()).asDisabled();
        Button pageBtn = ((Button) nav[1].getComponents().get(1)).asDisabled();
        Button rightBtn = ((Button) nav[1].getComponents().get(2)).asDisabled();

        nav[1].getComponents().set(0, leftBtn);
        nav[1].getComponents().set(1, pageBtn);
        nav[1].getComponents().set(2, rightBtn);

        return new ActionRow[] {
            nav[0], nav[1]
        };
    }

    private ActionRow[] getCompendiumCollectionComponents() {
        return getCompendiumCollectionComponents(false);
    }

    private ActionRow[] getCompendiumCollectionComponents(boolean isCompendium) {
        List<ActionRow> actionRows = new ArrayList<>();

        if (this.interactive.isAcknowledgeOpen()) {
            List<ItemComponent> okayRow = InteractiveUtil.makeComponents(
                this.interactive.getInteractionID(),
                this.COMPONENTS.get("okayBtn")
            );

            actionRows.add(ActionRow.of(okayRow));
            return actionRows.toArray(new ActionRow[0]);
        }

        if (this.interactive.isConfirmOpen()) {
            List<ItemComponent> confirmRow = InteractiveUtil.makeComponents(
                this.interactive.getInteractionID(),
                this.COMPONENTS.get("cancelBtn"),
                this.COMPONENTS.get("confirmBtn")
            );

            actionRows.add(ActionRow.of(confirmRow));
            return actionRows.toArray(new ActionRow[0]);
        }

        ActionRow[] nav = getNav();

        List<ItemComponent> interactRow = null;

        if (isCompendium) {
            interactRow = InteractiveUtil.makeComponents(
                this.interactive.getInteractionID(),
                this.COMPONENTS.get("boarFindBtn"),
                this.COMPONENTS.get("interactBtn")
            );
        }

        List<ItemComponent> filterSortRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("filterBtn"),
            this.COMPONENTS.get("sortBtn")
        );

        List<ItemComponent> selectRow = null;

        if (this.interactive.isFilterOpen()) {
            selectRow = getFilterRow();
        } else if (this.interactive.isSortOpen()) {
            selectRow = getSortRow();
        } else if (this.interactive.isInteractOpen() && this.interactive.getCurBoarEntry().getValue().amount() > 0) {
            selectRow = getInteractRow();
        }

        Button leftBtn = ((Button) nav[1].getComponents().getFirst()).asDisabled();
        Button pageBtn = ((Button) nav[1].getComponents().get(1)).asDisabled();
        Button rightBtn = ((Button) nav[1].getComponents().get(2)).asDisabled();
        Button filterBtn = ((Button) filterSortRow.getFirst());
        Button sortBtn = ((Button) filterSortRow.get(1));
        Button interactBtn = null;

        if (interactRow != null) {
            interactBtn = ((Button) interactRow.get(1)).asDisabled();
        }

        if (this.interactive.getPage() > 0) {
            leftBtn = leftBtn.withDisabled(false);
        }

        if (this.interactive.getMaxPage() > 0) {
            pageBtn = pageBtn.withDisabled(false);
        }

        if (this.interactive.getPage() < this.interactive.getMaxPage()) {
            rightBtn = rightBtn.withDisabled(false);
        }

        boolean userSelf = this.interactive.getUser().getId().equals(this.interactive.getBoarUser().getUserID());

        if (interactBtn != null && this.interactive.getCurBoarEntry().getValue().amount() > 0 && userSelf) {
            interactBtn = interactBtn.withDisabled(false);
        }

        if (this.interactive.getFilterBits() != 1) {
            filterBtn = filterBtn.withStyle(ButtonStyle.SUCCESS);
        }

        if (this.interactive.getSortVal() != SortType.RARITY_D) {
            sortBtn = sortBtn.withStyle(ButtonStyle.SUCCESS);
        }

        nav[1].getComponents().set(0, leftBtn);
        nav[1].getComponents().set(1, pageBtn);
        nav[1].getComponents().set(2, rightBtn);

        if (interactRow != null) {
            interactRow.set(1, interactBtn);
        }

        filterSortRow.set(0, filterBtn);
        filterSortRow.set(1, sortBtn);

        actionRows.add(nav[0]);
        actionRows.add(nav[1]);

        if (interactRow != null) {
            actionRows.add(ActionRow.of(interactRow));
        }

        actionRows.add(ActionRow.of(filterSortRow));

        if (selectRow != null) {
            actionRows.add(ActionRow.of(selectRow));
        }

        return actionRows.toArray(new ActionRow[0]);
    }

    private List<ItemComponent> getFilterRow() {
        if (this.filterOptions.isEmpty()) {
            this.makeSelectOptions(this.COMPONENTS.get("filterSelect"));
        }

        List<ItemComponent> selectRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("filterSelect")
        );

        for (int i=0; i<this.filterOptions.size(); i++) {
            SelectOption filterOption = this.filterOptions.get(i);

            if ((this.interactive.getFilterBits() >> i) % 2 == 1) {
                this.filterOptions.set(i, filterOption.withDefault(true));
                continue;
            }

            this.filterOptions.set(i, filterOption.withDefault(false));
        }

        List<SelectOption> selectOptions = new ArrayList<>(this.filterOptions);

        StringSelectMenu filterSelectMenu = (StringSelectMenu) selectRow.getFirst();
        selectRow.set(0, new StringSelectMenuImpl(
            filterSelectMenu.getId(),
            filterSelectMenu.getPlaceholder(),
            filterSelectMenu.getMinValues(),
            filterSelectMenu.getMaxValues(),
            filterSelectMenu.isDisabled(),
            selectOptions
        ));

        return selectRow;
    }

    private List<ItemComponent> getSortRow() {
        if (this.sortOptions.isEmpty()) {
            this.makeSelectOptions(this.COMPONENTS.get("sortSelect"));
        }

        List<ItemComponent> selectRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("sortSelect")
        );

        for (int i=0; i<this.sortOptions.size(); i++) {
            SelectOption sortOption = this.sortOptions.get(i);

            if (this.interactive.getSortVal() == SortType.values()[i]) {
                this.sortOptions.set(i, sortOption.withDefault(true));
                continue;
            }

            this.sortOptions.set(i, sortOption.withDefault(false));
        }

        StringSelectMenu sortSelectMenu = (StringSelectMenu) selectRow.getFirst();
        selectRow.set(0, new StringSelectMenuImpl(
            sortSelectMenu.getId(),
            sortSelectMenu.getPlaceholder(),
            sortSelectMenu.getMinValues(),
            sortSelectMenu.getMaxValues(),
            sortSelectMenu.isDisabled(),
            this.sortOptions
        ));

        return selectRow;
    }

    private List<ItemComponent> getInteractRow() {
        if (this.interactOptions.isEmpty()) {
            this.makeSelectOptions(this.COMPONENTS.get("interactSelect"));
        }

        List<ItemComponent> selectRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("interactSelect")
        );

        boolean canFavorite = this.interactive.getFavoriteID() == null ||
            !this.interactive.getFavoriteID().equals(this.interactive.getCurBoarEntry().getKey());

        List<SelectOption> selectOptions = new ArrayList<>();

        selectOptions.add(this.interactOptions.getFirst());

        if (!canFavorite) {
            selectOptions.set(0, this.interactOptions.getFirst().withLabel("Unfavorite"));
            selectOptions.set(0, selectOptions.getFirst().withDescription("Unfavorite this boar"));
        }

        RarityConfig curRarity = this.config.getRarityConfigs().get(this.interactive.getCurRarityKey());

        boolean cloneable = curRarity.getAvgClones() != -1 && this.interactive.getNumClone() > 0;
        boolean transmutable = curRarity.getChargesNeeded() != -1 &&
            curRarity.getChargesNeeded() <= this.interactive.getNumTransmute();

        BoarItemConfig boar = this.config.getItemConfig().getBoars().get(this.interactive.getCurBoarEntry().getKey());
        boolean canAnimate = boar.getStaticFile() != null;

        if (cloneable) {
            selectOptions.add(this.interactOptions.get(1));
        }

        if (transmutable) {
            selectOptions.add(this.interactOptions.get(2));
        }

        if (canAnimate) {
            selectOptions.add(this.interactOptions.get(3));
        }

        StringSelectMenu interactSelectMenu = (StringSelectMenu) selectRow.getFirst();
        selectRow.set(0, new StringSelectMenuImpl(
            interactSelectMenu.getId(),
            interactSelectMenu.getPlaceholder(),
            interactSelectMenu.getMinValues(),
            interactSelectMenu.getMaxValues(),
            interactSelectMenu.isDisabled(),
            selectOptions
        ));

        return selectRow;
    }

    private ActionRow[] getNav() {
        List<ItemComponent> viewSelect = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("viewSelect")
        );

        List<ItemComponent> navBtns = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("leftBtn"),
            this.COMPONENTS.get("pageBtn"),
            this.COMPONENTS.get("rightBtn"),
            this.COMPONENTS.get("refreshBtn")
        );

        for (int i=0; i<this.navOptions.size(); i++) {
            SelectOption navOption = this.navOptions.get(i);

            if (navOption.getValue().equals(this.interactive.getCurView().toString())) {
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
            ActionRow.of(viewSelect), ActionRow.of(navBtns)
        };
    }

    private void makeSelectOptions(IndivComponentConfig selectMenu) {
        List<SelectOption> options = new ArrayList<>();

        for (SelectOptionConfig selectOption : selectMenu.getOptions()) {
            SelectOption option = SelectOption.of(selectOption.getLabel(), selectOption.getValue())
                .withEmoji(InteractiveUtil.parseEmoji(selectOption.getEmoji()))
                .withDescription(selectOption.getDescription());
            options.add(option);
        }

        switch (selectMenu.getCustom_id()) {
            case "VIEW_SELECT" -> this.navOptions = options;
            case "FILTER_SELECT" ->  {
                Map<String, RarityConfig> rarities = this.config.getRarityConfigs();
                Set<String> ownedRarities = new HashSet<>();

                for (BoarInfo boarInfo : this.interactive.getOwnedBoars().values()) {
                    ownedRarities.add(boarInfo.rarityID());
                }

                int rarityBits = 4;
                for (Map.Entry<String, RarityConfig> rarityEntry : rarities.entrySet()) {
                    String rarityKey = rarityEntry.getKey();
                    RarityConfig rarity = rarityEntry.getValue();

                    if (!rarityEntry.getValue().isHidden() || ownedRarities.contains(rarityKey)) {
                        SelectOption option = SelectOption.of(rarity.getName(), Integer.toString(rarityBits))
                            .withEmoji(InteractiveUtil.parseEmoji(rarity.getEmoji()))
                            .withDescription("Filter by %s Boars".formatted(rarity.getName()));
                        options.add(option);
                        rarityBits *= 2;
                    }
                }

                this.COMPONENTS.get("filterSelect").setMax_values(options.size());
                this.filterOptions = options;
            }
            case "SORT_SELECT" -> this.sortOptions = options;
            case "INTERACT_SELECT" -> this.interactOptions = options;
        }
    }
}
