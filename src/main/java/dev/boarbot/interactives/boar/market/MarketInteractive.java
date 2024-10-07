package dev.boarbot.interactives.boar.market;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.market.*;
import dev.boarbot.util.generators.ImageGenerator;
import dev.boarbot.util.generators.MarketImageGenerator;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarketInteractive extends ModalInteractive implements Synchronizable {
    MarketView curView = MarketView.OVERVIEW;
    List<String> itemIDs;
    int itemPage = 0;
    int maxItemPage = 0;
    private RarityConfig rarity;
    String focusedID;

    private BoarUser boarUser;

    boolean confirmOpen = false;
    String confirmString;
    boolean acknowledgeOpen = false;
    String acknowledgeString;

    boolean isBuying = false;
    int amount = 0;
    long cost = 0;

    private final MarketComponentsGetter componentsGetter;

    public final static Map<String, MarketData> cachedMarketData = new ConcurrentHashMap<>();

    private final static Map<String, ModalConfig> MODALS = CONFIG.getModalConfig();
    private static final int ITEMS_PER_PAGE = 15;

    public MarketInteractive(SlashCommandInteractionEvent event) {
        super(event);

        this.componentsGetter = new MarketComponentsGetter(this);

        try {
            this.boarUser = BoarUserFactory.getBoarUser(this.user);
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to update data", exception);
            return;
        }

        if (event.getOption("search") != null) {
            this.setItemsFromSearch(Objects.requireNonNull(event.getOption("search")).getAsString());
        }
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent == null) {
            this.sendResponse();
            return;
        }

        if (this.getModalHandler() != null) {
            this.getModalHandler().stop();
        }

        String compID = compEvent.getComponentId().split(",")[1];

        Set<String> modalPossibleIDs = new HashSet<>(
            Arrays.asList("PAGE", "SEARCH", "BUY", "SELL")
        );

        if (!modalPossibleIDs.contains(compID)) {
            compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(compEvent, this, e));
        }

        Log.debug(
            this.user,
            this.getClass(),
            "Page: %d | View: %s | Component: %s".formatted(this.itemPage, this.curView.toString(), compID)
        );

        switch (compID) {
            case "POWERUPS" -> {
                this.curView = MarketView.ITEMS;
                this.setItemsPowerups();
                Log.debug(this.user, this.getClass(), "Selected board: " + this.curView);
            }

            case "BOARS" -> {
                this.curView = MarketView.RARITIES;
                Log.debug(this.user, this.getClass(), "Selected board: " + this.curView);
            }

            case "SEARCH" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("findBoar"), compEvent);
                this.setModalHandler(new ModalHandler(compEvent, this));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent search item modal");
            }

            case "BACK" -> {
                if (this.acknowledgeOpen) {
                    this.acknowledgeOpen = false;
                } else {
                    switch (this.curView) {
                        case RARITIES -> {
                            this.rarity = null;
                            this.curView = MarketView.OVERVIEW;
                        }

                        case ITEMS -> {
                            this.itemPage = 0;

                            if (this.rarity != null) {
                                this.curView = MarketView.RARITIES;
                            } else {
                                this.curView = MarketView.OVERVIEW;
                            }
                        }

                        case FOCUSED -> {
                            this.focusedID = null;
                            this.curView = MarketView.ITEMS;
                        }
                    }
                }
            }

            case "ITEM_SELECT" -> {
                this.curView = MarketView.FOCUSED;
                this.focusedID = ((StringSelectInteractionEvent) compEvent).getValues().getFirst();
            }

            case "RARITY_SELECT" -> {
                this.curView = MarketView.ITEMS;
                this.rarity = RARITIES.get(((StringSelectInteractionEvent) compEvent).getValues().getFirst());
                this.setItemsFromRarity();
                this.maxItemPage = Math.max((this.itemIDs.size()-1) / ITEMS_PER_PAGE, 0);
            }

            case "LEFT" -> this.itemPage--;

            case "PAGE" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("pageInput"), compEvent);
                this.setModalHandler(new ModalHandler(compEvent, this));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent page input modal");
            }

            case "RIGHT" -> this.itemPage++;

            case "BUY" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("buyInput"), compEvent);
                this.setModalHandler(new ModalHandler(compEvent, this));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent buy input modal");
            }

            case "SELL" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("sellInput"), compEvent);
                this.setModalHandler(new ModalHandler(compEvent, this));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent sell input modal");
            }

            case "CONFIRM" -> {
                this.confirmOpen = false;
                this.boarUser.passSynchronizedAction(this);
            }

            case "CANCEL" -> this.confirmOpen = false;
        }

        this.sendResponse();
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(modalEvent, this, e));

        switch (modalEvent.getModalId().split(",")[2]) {
            case "PAGE_INPUT" -> {
                String pageInputRaw = modalEvent.getValues().getFirst().getAsString();
                Log.debug(this.user, this.getClass(), "Page input: " + pageInputRaw);

                try {
                    String pageInput = pageInputRaw.replaceAll("[^0-9]+", "");
                    this.itemPage = Math.min(Math.max(Integer.parseInt(pageInput)-1, 0), this.maxItemPage);
                } catch (NumberFormatException exception) {
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                }
            }

            case "FIND_BOAR" -> {
                String searchInput = modalEvent.getValues().getFirst().getAsString();
                Log.debug(this.user, this.getClass(), "Search input: " + searchInput);
                this.setItemsFromSearch(searchInput);
            }

            case "BUY_INPUT" -> {
                String buyInputRaw = modalEvent.getValues().getFirst().getAsString();
                Log.debug(this.user, this.getClass(), "Buy amount input: " + buyInputRaw);

                try (Connection connection = DataUtil.getConnection()) {
                    String buyInput = buyInputRaw.replaceAll("[^0-9]+", "");
                    int input = Integer.parseInt(buyInput);

                    if (input == 0) {
                        throw new NumberFormatException();
                    }

                    MarketData marketData = MarketDataUtil.getMarketDataItem(this.focusedID, true, connection);
                    MarketTransactionData buyData = MarketDataUtil.calculateBuyCost(this.focusedID, marketData, input);
                    long userBucks = this.boarUser.baseQuery().getBucks(connection);

                    if (userBucks < buyData.cost()) {
                        this.acknowledgeOpen = true;
                        this.acknowledgeString = STRS.getMarketNoBucks().formatted(buyData.cost(), userBucks);

                        this.execute(null);
                        return;
                    }

                    String itemStr = POWS.containsKey(this.focusedID)
                        ? "<>powerup<>" + (buyData.amount() == 1
                            ? POWS.get(this.focusedID).getName()
                            : POWS.get(this.focusedID).getPluralName())
                        : "<>" + BoarUtil.findRarityKey(this.focusedID) + "<>" + (buyData.amount() == 1
                            ? BOARS.get(this.focusedID).getName()
                            : BOARS.get(this.focusedID).getPluralName());

                    this.isBuying = true;
                    this.cost = buyData.cost();
                    this.amount = buyData.amount();

                    this.confirmOpen = true;
                    this.confirmString = STRS.getMarketBuyConfirm().formatted(
                        buyData.amount(),
                        itemStr,
                        buyData.cost(),
                        marketData.stock(),
                        buyData.stock(),
                        marketData.sellPrice(),
                        buyData.sellPrice(),
                        marketData.buyPrice(),
                        buyData.buyPrice()
                    );
                } catch (NumberFormatException exception) {
                    this.acknowledgeOpen = true;
                    this.acknowledgeString = STRS.getInvalidInput();
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                } catch (SQLException exception) {
                    this.stop(StopType.EXCEPTION);
                    Log.error(this.user, this.getClass(), "Failed to get bucks data", exception);
                    return;
                }
            }

            case "SELL_INPUT" -> {
                String sellInputRaw = modalEvent.getValues().getFirst().getAsString();
                Log.debug(this.user, this.getClass(), "Sell amount input: " + sellInputRaw);

                try (Connection connection = DataUtil.getConnection()) {
                    String sellInput = sellInputRaw.replaceAll("[^0-9]+", "");
                    int input = Integer.parseInt(sellInput);

                    if (input == 0) {
                        throw new NumberFormatException();
                    }

                    boolean isPowerup = POWS.containsKey(this.focusedID);
                    int userAmount = isPowerup
                        ? this.boarUser.powQuery().getPowerupAmount(connection, this.focusedID)
                        : this.boarUser.boarQuery().getBoarAmount(this.focusedID, connection);

                    if (userAmount == 0) {
                        this.acknowledgeOpen = true;
                        this.acknowledgeString = STRS.getMarketNoItems();

                        this.execute(null);
                        return;
                    }

                    input = Math.min(input, userAmount);

                    MarketData marketData = MarketDataUtil.getMarketDataItem(this.focusedID, true, connection);
                    MarketTransactionData sellData = MarketDataUtil.calculateSellCost(
                        this.focusedID, marketData, input
                    );

                    String itemStr = isPowerup
                        ? "<>powerup<>" + (sellData.amount() == 1
                            ? POWS.get(this.focusedID).getName()
                            : POWS.get(this.focusedID).getPluralName())
                        : "<>" + BoarUtil.findRarityKey(this.focusedID) + "<>" + (sellData.amount() == 1
                            ? BOARS.get(this.focusedID).getName()
                            : BOARS.get(this.focusedID).getPluralName());

                    this.cost = sellData.cost();
                    this.amount = sellData.amount();

                    this.confirmOpen = true;
                    this.confirmString = STRS.getMarketSellConfirm().formatted(
                        sellData.amount(),
                        itemStr,
                        sellData.cost(),
                        marketData.stock(),
                        sellData.stock(),
                        marketData.sellPrice(),
                        sellData.sellPrice(),
                        marketData.buyPrice(),
                        sellData.buyPrice()
                    );
                } catch (NumberFormatException exception) {
                    this.acknowledgeOpen = true;
                    this.acknowledgeString = STRS.getInvalidInput();
                    Log.debug(this.user, this.getClass(), "Invalid modal input");
                } catch (SQLException exception) {
                    this.stop(StopType.EXCEPTION);
                    Log.error(this.user, this.getClass(), "Failed to get bucks data", exception);
                    return;
                }
            }
        }

        this.execute(null);
    }

    private void sendResponse() {
        try {
            ImageGenerator marketImageGen = new MarketImageGenerator(
                this.itemPage, this.curView, this.itemIDs, this.focusedID
            ).generate();

            FileUpload fileUpload;

            if (this.confirmOpen) {
                fileUpload = new OverlayImageGenerator(marketImageGen.getImage(), this.confirmString).generate()
                    .getFileUpload();
            } else if (this.acknowledgeOpen) {
                fileUpload = new OverlayImageGenerator(marketImageGen.getImage(), this.acknowledgeString).generate()
                    .getFileUpload();
            } else {
                fileUpload = marketImageGen.getFileUpload();
            }

            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(fileUpload).setComponents(this.getCurComponents());

            this.updateInteractive(false, editedMsg.build());
        } catch (IOException | URISyntaxException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate market image", exception);
        }
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        if (this.isBuying) {
            try (Connection connection = DataUtil.getConnection()) {
                this.doBuy(boarUser, connection);
            } catch (SQLException exception) {
                this.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to purchase " + this.focusedID, exception);
            }
        } else {
            try (Connection connection = DataUtil.getConnection()) {
                this.doSell(boarUser, connection);
            } catch (SQLException exception) {
                this.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to sell " + this.focusedID, exception);
            }
        }

        this.confirmOpen = false;
    }

    private void doBuy(BoarUser boarUser, Connection connection) throws SQLException {
        long userBucks = this.boarUser.baseQuery().getBucks(connection);

        if (userBucks < this.cost) {
            this.acknowledgeOpen = true;
            this.acknowledgeString = STRS.getMarketNoBucks().formatted(this.cost, userBucks);

            this.execute(null);
            return;
        }

        MarketTransactionFail failType = MarketDataUtil.updateMarket(
            MarketUpdateType.BUY_ITEM, this.focusedID, this.amount, this.cost, boarUser, connection
        );

        switch (failType) {
            case STOCK -> {
                this.acknowledgeOpen = true;
                this.acknowledgeString = STRS.getMarketStockChange();

                this.execute(null);
                return;
            }

            case COST -> {
                this.acknowledgeOpen = true;
                this.acknowledgeString = STRS.getMarketCostChange();

                this.execute(null);
                return;
            }

            case STORAGE -> {
                this.acknowledgeOpen = true;
                this.acknowledgeString = STRS.getMarketNoStorage();

                this.execute(null);
                return;
            }

            case null -> {}
        }

        Log.info(
            this.user, this.getClass(), "Purchased %,d %s for $%,d".formatted(this.amount, this.focusedID, this.cost)
        );

        String itemStr = POWS.containsKey(this.focusedID)
            ? "<>powerup<>" + (this.amount == 1
                ? POWS.get(this.focusedID).getName()
                : POWS.get(this.focusedID).getPluralName())
            : "<>" + BoarUtil.findRarityKey(this.focusedID) + "<>" + (this.amount == 1
                ? BOARS.get(this.focusedID).getName()
                : BOARS.get(this.focusedID).getPluralName());

        this.isBuying = false;

        this.acknowledgeOpen = true;
        this.acknowledgeString = STRS.getMarketBuySuccess().formatted(this.amount, itemStr, this.cost);
    }

    private void doSell(BoarUser boarUser, Connection connection) throws SQLException {
        int userAmount = POWS.containsKey(this.focusedID)
            ? this.boarUser.powQuery().getPowerupAmount(connection, this.focusedID)
            : this.boarUser.boarQuery().getBoarAmount(this.focusedID, connection);

        if (userAmount < this.amount) {
            this.acknowledgeOpen = true;
            this.acknowledgeString = STRS.getMarketNoItems();

            this.execute(null);
            return;
        }

        MarketTransactionFail failType = MarketDataUtil.updateMarket(
            MarketUpdateType.SELL_ITEM, this.focusedID, this.amount, this.cost, boarUser, connection
        );

        switch (failType) {
            case COST -> {
                this.acknowledgeOpen = true;
                this.acknowledgeString = STRS.getMarketCostChange();

                this.execute(null);
                return;
            }

            case null -> {}

            default -> {}
        }

        Log.info(this.user, this.getClass(), "Sold %,d %s for $%,d".formatted(this.amount, this.focusedID, this.cost));

        String itemStr = POWS.containsKey(this.focusedID)
            ? "<>powerup<>" + (this.amount == 1
                ? POWS.get(this.focusedID).getName()
                : POWS.get(this.focusedID).getPluralName())
            : "<>" + BoarUtil.findRarityKey(this.focusedID) + "<>" + (this.amount == 1
                ? BOARS.get(this.focusedID).getName()
                : BOARS.get(this.focusedID).getPluralName());

        this.isBuying = false;

        this.acknowledgeOpen = true;
        this.acknowledgeString = STRS.getMarketSellSuccess().formatted(this.amount, itemStr, this.cost);
    }

    @Override
    public ActionRow[] getCurComponents() {
        return this.componentsGetter.getComponents();
    }

    private void setItemsFromSearch(String input) {
        List<String> searchedItemIDs = new ArrayList<>();

        String cleanInput = input.replaceAll(" ", "").toLowerCase();

        for (String powerupID : POWS.keySet()) {
            if (!cachedMarketData.containsKey(powerupID)) {
                continue;
            }

            for (String searchTerm : POWS.get(powerupID).getSearchTerms()) {
                if (cleanInput.equals(searchTerm)) {
                    searchedItemIDs.add(powerupID);
                }
            }
        }

        for (String boarID : BOARS.keySet()) {
            if (BOARS.get(boarID).isBlacklisted() || !cachedMarketData.containsKey(boarID)) {
                continue;
            }

            for (String searchTerm : BOARS.get(boarID).getSearchTerms()) {
                if (cleanInput.equals(searchTerm)) {
                    searchedItemIDs.add(boarID);
                }
            }
        }

        while (!cleanInput.isEmpty()) {
            for (String powerupID : POWS.keySet()) {
                boolean shouldAdd = !searchedItemIDs.contains(powerupID) &&
                    POWS.get(powerupID).getName().replaceAll(" ", "").toLowerCase().startsWith(cleanInput) &&
                    cachedMarketData.containsKey(powerupID);

                if (shouldAdd) {
                    searchedItemIDs.add(powerupID);
                }
            }

            for (RarityConfig rarity : RARITIES.values()) {
                for (String boarID : rarity.getBoars()) {
                    boolean shouldAdd = !BOARS.get(boarID).isBlacklisted() &&
                        !searchedItemIDs.contains(boarID) &&
                        BOARS.get(boarID).getName().replaceAll(" ", "").toLowerCase().startsWith(cleanInput) &&
                        cachedMarketData.containsKey(boarID);

                    if (shouldAdd) {
                        searchedItemIDs.add(boarID);
                    }
                }
            }

            cleanInput = cleanInput.substring(0, cleanInput.length()-1);
        }

        this.curView = MarketView.ITEMS;
        this.itemIDs = searchedItemIDs;
        this.rarity = null;

        this.itemPage = 0;
        this.maxItemPage = Math.max((this.itemIDs.size()-1) / ITEMS_PER_PAGE, 0);
    }

    private void setItemsFromRarity() {
        List<String> rarityItems = new ArrayList<>();

        for (String boarID : this.rarity.getBoars()) {
            if (!BOARS.get(boarID).isBlacklisted() && cachedMarketData.containsKey(boarID)) {
                rarityItems.add(boarID);
            }
        }

        this.itemIDs = rarityItems;
    }

    private void setItemsPowerups() {
        List<String> powItems = new ArrayList<>();

        for (String powerupID : POWS.keySet()) {
            if (cachedMarketData.containsKey(powerupID)) {
                powItems.add(powerupID);
            }
        }

        this.itemIDs = powItems;
    }
}
