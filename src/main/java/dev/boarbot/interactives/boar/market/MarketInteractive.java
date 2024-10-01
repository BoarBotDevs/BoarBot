package dev.boarbot.interactives.boar.market;

import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.data.BoarDataUtil;
import dev.boarbot.util.data.DataUtil;
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
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class MarketInteractive extends ModalInteractive {
    MarketView curView = MarketView.OVERVIEW;
    List<String> itemIDs;
    int itemPage = 0;
    int maxItemPage = 0;
    private RarityConfig rarity;
    private String focusedID;

    private final MarketComponentsGetter componentsGetter;

    private final Set<String> validBoarIDs = new HashSet<>();

    private final static Map<String, ModalConfig> MODALS = CONFIG.getModalConfig();
    private static final int ITEMS_PER_PAGE = 12;

    public MarketInteractive(SlashCommandInteractionEvent event) {
        super(event);

        this.componentsGetter = new MarketComponentsGetter(this);

        try (Connection connection = DataUtil.getConnection()) {
            for (String boarID : BOARS.keySet()) {
                if (!BOARS.get(boarID).isBlacklisted() && BoarDataUtil.boarExists(boarID, connection)) {
                    this.validBoarIDs.add(boarID);
                }
            }
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to get boars that exist", exception);
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

        if (!compID.equals("SEARCH") && !compID.equals("PAGE")) {
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
                Modal modal = new ModalImpl(
                    ModalUtil.makeModalID(MODALS.get("findBoar").getId(), compEvent),
                    MODALS.get("pageInput").getTitle(),
                    ModalUtil.makeModalComponents(MODALS.get("findBoar").getComponents())
                );

                this.setModalHandler(new ModalHandler(compEvent, this));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));

                Log.debug(this.user, this.getClass(), "Sent search item modal");
            }

            case "BACK" -> {
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
                Modal modal = new ModalImpl(
                    ModalUtil.makeModalID(MODALS.get("pageInput").getId(), compEvent),
                    MODALS.get("pageInput").getTitle(),
                    ModalUtil.makeModalComponents(MODALS.get("pageInput").getComponents())
                );

                this.setModalHandler(new ModalHandler(compEvent, this));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));

                Log.debug(this.user, this.getClass(), "Sent page input modal");
            }

            case "RIGHT" -> this.itemPage++;
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
        }

        this.execute(null);
    }

    private void sendResponse() {
        MessageEditBuilder editedMsg = new MessageEditBuilder().setComponents(this.getCurComponents());
        this.updateInteractive(false, editedMsg.build());
    }

    @Override
    public ActionRow[] getCurComponents() {
        return this.componentsGetter.getComponents();
    }

    private void setItemsFromSearch(String input) {
        List<String> searchedItemIDs = new ArrayList<>();

        String cleanInput = input.replaceAll(" ", "").toLowerCase();

        for (String powerupID : POWS.keySet()) {
            for (String searchTerm : POWS.get(powerupID).getSearchTerms()) {
                if (cleanInput.equals(searchTerm)) {
                    searchedItemIDs.add(powerupID);
                }
            }
        }

        for (String boarID : this.validBoarIDs) {
            for (String searchTerm : BOARS.get(boarID).getSearchTerms()) {
                if (cleanInput.equals(searchTerm)) {
                    searchedItemIDs.add(boarID);
                }
            }
        }

        while (!cleanInput.isEmpty()) {
            for (String powerupID : POWS.keySet()) {
                boolean shouldAdd = !searchedItemIDs.contains(powerupID) &&
                    POWS.get(powerupID).getName().replaceAll(" ", "").toLowerCase().startsWith(cleanInput);

                if (shouldAdd) {
                    searchedItemIDs.add(powerupID);
                }
            }

            for (RarityConfig rarity : RARITIES.values()) {
                for (String boarID : rarity.getBoars()) {
                    boolean shouldAdd = this.validBoarIDs.contains(boarID) &&
                        !searchedItemIDs.contains(boarID) &&
                        BOARS.get(boarID).getName().replaceAll(" ", "").toLowerCase().startsWith(cleanInput);

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
        List<String> curBoarIDs = new ArrayList<>();

        for (String boarID : this.rarity.getBoars()) {
            if (this.validBoarIDs.contains(boarID)) {
                curBoarIDs.add(boarID);
            }
        }

        this.itemIDs = curBoarIDs;
    }

    private void setItemsPowerups() {
        this.itemIDs = POWS.keySet().stream().toList();
    }
}
