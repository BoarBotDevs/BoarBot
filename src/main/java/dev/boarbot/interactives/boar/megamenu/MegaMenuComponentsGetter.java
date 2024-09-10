package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.util.interactive.InteractiveUtil;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
    private List<SelectOption> powOptions = new ArrayList<>();

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getMegaMenu();

    public MegaMenuComponentsGetter(MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.makeSelectOptions(this.COMPONENTS.get("viewSelect"));
    }

    public ActionRow[] getComponents() {
        if (this.interactive.isAcknowledgeOpen()) {
            List<ItemComponent> backRow = InteractiveUtil.makeComponents(
                this.interactive.getInteractionID(),
                this.COMPONENTS.get("backBtn")
            );

            return new ActionRow[] {ActionRow.of(backRow)};
        }

        if (this.interactive.isConfirmOpen()) {
            List<ItemComponent> confirmRow = InteractiveUtil.makeComponents(
                this.interactive.getInteractionID(),
                this.COMPONENTS.get("cancelBtn"),
                this.COMPONENTS.get("confirmBtn")
            );

            return new ActionRow[] {ActionRow.of(confirmRow)};
        }

        return switch (this.interactive.getCurView()) {
            case MegaMenuView.PROFILE, MegaMenuView.STATS, MegaMenuView.BADGES -> this.getNav();
            case MegaMenuView.COLLECTION -> this.getCompendiumCollectionComponents();
            case MegaMenuView.COMPENDIUM -> this.getCompendiumCollectionComponents(true);
            case MegaMenuView.EDITIONS -> this.getEditionsComponents();
            case MegaMenuView.POWERUPS -> this.getPowerupsComponents();
            case MegaMenuView.QUESTS -> this.getQuestsComponents();
        };
    }

    private ActionRow[] getCompendiumCollectionComponents() {
        return this.getCompendiumCollectionComponents(false);
    }

    private ActionRow[] getCompendiumCollectionComponents(boolean isCompendium) {
        List<ActionRow> actionRows = new ArrayList<>();
        ActionRow[] nav = this.getNav();

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
            selectRow = this.getFilterRow();
        } else if (this.interactive.isSortOpen()) {
            selectRow = this.getSortRow();
        } else if (this.interactive.isInteractOpen()) {
            selectRow = this.getInteractRow();
        }

        Button interactBtn = null;

        if (interactRow != null) {
            interactBtn = ((Button) interactRow.get(1)).asDisabled();
        }

        boolean userSelf = this.interactive.getUser().getId().equals(this.interactive.getBoarUser().getUserID());

        if (interactBtn != null && this.interactive.getCurBoarEntry().getValue().getAmount() > 0 && userSelf) {
            interactBtn = interactBtn.withDisabled(false);
        }

        if (interactRow != null) {
            interactRow.set(1, interactBtn);
        }

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

    private ActionRow[] getEditionsComponents() {
        ActionRow[] nav = this.getNav();

        List<ItemComponent> backRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("backBtn")
        );

        String rarityEmoji = this.config.getRarityConfigs().get(this.interactive.getCurRarityKey()).getEmoji();
        String boarName = this.config.getItemConfig().getBoars().get(
            this.interactive.getCurBoarEntry().getKey()
        ).getName();

        List<SelectOption> selectOptions = new ArrayList<>(this.navOptions);
        selectOptions.add(
            SelectOption.of(boarName + " Editions", MegaMenuView.EDITIONS.toString())
                .withEmoji(InteractiveUtil.parseEmoji(rarityEmoji))
                .withDescription("Viewing owned editions for %s".formatted(boarName))
                .withDefault(true)
        );

        StringSelectMenu navSelectMenu = (StringSelectMenu) nav[0].getComponents().getFirst();
        nav[0].getComponents().set(0, new StringSelectMenuImpl(
            navSelectMenu.getId(),
            navSelectMenu.getPlaceholder(),
            navSelectMenu.getMinValues(),
            navSelectMenu.getMaxValues(),
            navSelectMenu.isDisabled(),
            selectOptions
        ));

        return new ActionRow[] {
            nav[0], nav[1], ActionRow.of(backRow)
        };
    }

    private ActionRow[] getPowerupsComponents() {
        ActionRow[] nav = this.getNav();

        if (this.powOptions.isEmpty()) {
            this.makeSelectOptions(this.COMPONENTS.get("powSelect"));
        }

        List<ItemComponent> selectRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            this.COMPONENTS.get("powSelect")
        );

        return new ActionRow[] {nav[0], nav[1], ActionRow.of(selectRow)};
    }

    private ActionRow[] getQuestsComponents() {
        return this.getNav();
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

        StringSelectMenu filterSelectMenu = (StringSelectMenu) selectRow.getFirst();
        selectRow.set(0, new StringSelectMenuImpl(
            filterSelectMenu.getId(),
            filterSelectMenu.getPlaceholder(),
            filterSelectMenu.getMinValues(),
            filterSelectMenu.getMaxValues(),
            filterSelectMenu.isDisabled(),
            this.filterOptions
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

        List<SelectOption> selectOptions = new ArrayList<>(this.interactOptions);

        if (!canFavorite) {
            selectOptions.set(0, this.interactOptions.getFirst().withLabel("Unfavorite"));
            selectOptions.set(0, selectOptions.getFirst().withDescription("Unfavorite this boar"));
        }

        RarityConfig curRarity = this.config.getRarityConfigs().get(this.interactive.getCurRarityKey());

        boolean cloneable = curRarity.getAvgClones() != 0 && this.interactive.getNumClone() > 0;
        boolean transmutable = curRarity.getChargesNeeded() != 0 &&
            curRarity.getChargesNeeded() <= this.interactive.getNumTransmute();

        if (!cloneable) {
            selectOptions.remove(1);
        }

        if (cloneable && !transmutable) {
            selectOptions.remove(2);
        } else if (!cloneable && !transmutable) {
            selectOptions.remove(1);
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

        Button leftBtn = ((Button) navBtns.getFirst()).asDisabled();
        Button pageBtn = ((Button) navBtns.get(1)).asDisabled();
        Button rightBtn = ((Button) navBtns.get(2)).asDisabled();

        if (this.interactive.getPage() > 0) {
            leftBtn = leftBtn.withDisabled(false);
        }

        if (this.interactive.getMaxPage() > 0) {
            pageBtn = pageBtn.withDisabled(false);
        }

        if (this.interactive.getPage() < this.interactive.getMaxPage()) {
            rightBtn = rightBtn.withDisabled(false);
        }

        navBtns.set(0, leftBtn);
        navBtns.set(1, pageBtn);
        navBtns.set(2, rightBtn);

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
                    ownedRarities.add(boarInfo.getRarityID());
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
            case "POW_SELECT" -> this.powOptions = options;
        }
    }
}
