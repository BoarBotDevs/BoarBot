package dev.boarbot.interactives.boar.market;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.items.BaseItemConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.interactive.InteractiveUtil;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketComponentsGetter implements Configured {
    private final MarketInteractive interactive;

    private static final Map<String, IndivComponentConfig> COMPONENTS = CONFIG.getComponentConfig().getMarket();
    private List<SelectOption> rarityOptions;

    private static final int ITEMS_PER_PAGE = 15;

    public MarketComponentsGetter(MarketInteractive interactive) {
        this.interactive = interactive;
        this.makeRarityOptions();
    }

    public ActionRow[] getComponents() {
        if (this.interactive.acknowledgeOpen) {
            List<ItemComponent> backRow = InteractiveUtil.makeComponents(
                this.interactive.getInteractionID(), COMPONENTS.get("backBtn")
            );
            return new ActionRow[] {ActionRow.of(backRow)};
        }

        if (this.interactive.confirmOpen) {
            List<ItemComponent> confirmRow = InteractiveUtil.makeComponents(
                this.interactive.getInteractionID(), COMPONENTS.get("cancelBtn"), COMPONENTS.get("confirmBtn")
            );
            return new ActionRow[] {ActionRow.of(confirmRow)};
        }

        return switch (this.interactive.curView) {
            case OVERVIEW -> getOverviewComponents();
            case RARITIES -> getRaritiesComponents();
            case ITEMS -> getItemComponents();
            case FOCUSED -> getFocusedComponents();
        };
    }

    private ActionRow[] getOverviewComponents() {
        List<ItemComponent> navBtns = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(), COMPONENTS.get("powerupsBtn"), COMPONENTS.get("boarsBtn")
        );

        List<ItemComponent> searchRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(), COMPONENTS.get("searchBtn")
        );

        return new ActionRow[] {ActionRow.of(navBtns), ActionRow.of(searchRow)};
    }

    private ActionRow[] getRaritiesComponents() {
        List<ItemComponent> navSelect = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(), COMPONENTS.get("raritySelect")
        );

        List<ItemComponent> searchRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(), COMPONENTS.get("backBtn"), COMPONENTS.get("searchBtn")
        );

        StringSelectMenu navSelectMenu = (StringSelectMenu) navSelect.getFirst();
        navSelect.set(0, new StringSelectMenuImpl(
            navSelectMenu.getId(),
            navSelectMenu.getPlaceholder(),
            navSelectMenu.getMinValues(),
            navSelectMenu.getMaxValues(),
            navSelectMenu.isDisabled(),
            this.rarityOptions
        ));

        return new ActionRow[] {ActionRow.of(navSelect), ActionRow.of(searchRow)};
    }

    private ActionRow[] getItemComponents() {
        List<ItemComponent> navSelect = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(), COMPONENTS.get("itemSelect")
        );

        List<ItemComponent> navBtns = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            COMPONENTS.get("leftBtn"),
            COMPONENTS.get("pageBtn"),
            COMPONENTS.get("rightBtn")
        );

        List<ItemComponent> searchRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            COMPONENTS.get("backBtn"),
            COMPONENTS.get("searchBtn"),
            COMPONENTS.get("refreshBtn")
        );

        Button leftBtn = ((Button) navBtns.getFirst()).asDisabled();
        Button pageBtn = ((Button) navBtns.get(1)).asDisabled();
        Button rightBtn = ((Button) navBtns.get(2)).asDisabled();

        if (this.interactive.itemPage > 0) {
            leftBtn = leftBtn.withDisabled(false);
        }

        if (this.interactive.maxItemPage > 0) {
            pageBtn = pageBtn.withDisabled(false);
        }

        if (this.interactive.itemPage < this.interactive.maxItemPage) {
            rightBtn = rightBtn.withDisabled(false);
        }

        navBtns.set(0, leftBtn);
        navBtns.set(1, pageBtn);
        navBtns.set(2, rightBtn);

        List<SelectOption> itemOptions = this.getItemOptions();
        StringSelectMenu navSelectMenu = (StringSelectMenu) navSelect.getFirst();
        navSelect.set(0, new StringSelectMenuImpl(
            navSelectMenu.getId(),
            navSelectMenu.getPlaceholder(),
            navSelectMenu.getMinValues(),
            navSelectMenu.getMaxValues(),
            navSelectMenu.isDisabled(),
            itemOptions
        ));

        if (itemOptions.isEmpty()) {
            return new ActionRow[] {ActionRow.of(navBtns), ActionRow.of(searchRow)};
        }

        return new ActionRow[] {ActionRow.of(navSelect), ActionRow.of(navBtns), ActionRow.of(searchRow)};
    }

    private ActionRow[] getFocusedComponents() {
        List<ItemComponent> buySellRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(), COMPONENTS.get("buyBtn"), COMPONENTS.get("sellBtn")
        );

        List<ItemComponent> searchRow = InteractiveUtil.makeComponents(
            this.interactive.getInteractionID(),
            COMPONENTS.get("backBtn"),
            COMPONENTS.get("searchBtn"),
            COMPONENTS.get("refreshBtn")
        );

        Button buyBtn = ((Button) buySellRow.getFirst()).asDisabled();

        boolean canPurchase = MarketInteractive.cachedMarketData.get(this.interactive.focusedID) != null &&
            !MarketInteractive.cachedMarketData.get(this.interactive.focusedID).isEmpty();
        if (canPurchase) {
            buyBtn = buyBtn.withDisabled(false);
        }

        buySellRow.set(0, buyBtn);

        return new ActionRow[] {ActionRow.of(buySellRow), ActionRow.of(searchRow)};
    }

    private void makeRarityOptions() {
        List<SelectOption> options = new ArrayList<>();

        for (String rarityID : RARITIES.keySet()) {
            RarityConfig rarity = RARITIES.get(rarityID);

            if (rarity.getAvgClones() == 0) {
                continue;
            }

            SelectOption option = SelectOption.of(rarity.getName(), rarityID)
                .withEmoji(InteractiveUtil.parseEmoji(rarity.getEmoji()));
            options.add(option);
        }

        this.rarityOptions = options;
    }

    private List<SelectOption> getItemOptions() {
        List<SelectOption> options = new ArrayList<>();

        int startIndex = this.interactive.itemPage*ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, this.interactive.itemIDs.size());

        for (int i=startIndex; i<endIndex; i++) {
            String itemID = this.interactive.itemIDs.get(i);
            boolean isPowerup = POWS.containsKey(itemID);
            BaseItemConfig item = isPowerup ? POWS.get(itemID) : BOARS.get(itemID);
            String emoji = isPowerup
                ? POWS.get(itemID).getEmoji()
                : RARITIES.get(BoarUtil.findRarityKey(itemID)).getEmoji();

            SelectOption option = SelectOption.of(item.getName(), itemID).withEmoji(InteractiveUtil.parseEmoji(emoji));
            options.add(option);
        }

        return options;
    }
}
