package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.generators.megamenu.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

class MegaMenuGeneratorMaker {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    private final MegaMenuInteractive interactive;
    private MegaMenuView view;

    public MegaMenuGeneratorMaker(MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.view = this.interactive.getCurView();
    }

    public MegaMenuGenerator make() throws SQLException {
        this.view = this.interactive.getCurView();

        return switch (this.interactive.getCurView()) {
            case MegaMenuView.PROFILE -> this.makeProfileGen();
            case MegaMenuView.COLLECTION -> this.makeCollectionGen();
            case MegaMenuView.COMPENDIUM -> this.makeCompendiumGen();
            case MegaMenuView.EDITIONS -> this.makeEditionsGen();
            case MegaMenuView.STATS -> this.makeStatsGen();
            case MegaMenuView.POWERUPS -> this.makeCollectionGen();
            case MegaMenuView.QUESTS -> this.makeCollectionGen();
            case MegaMenuView.BADGES -> this.makeCollectionGen();
        };
    }

    public MegaMenuGenerator makeProfileGen() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.setProfileData(this.interactive.getBoarUser().getProfileData(connection));
                this.interactive.getViewsToUpdateData().put(this.view, true);
            }
        }

        this.interactive.setMaxPage(0);
        if (this.interactive.getPage() > this.interactive.getMaxPage()) {
            this.interactive.setPrevPage(this.interactive.getPage());
            this.interactive.setPage(this.interactive.getMaxPage());
        }

        return new ProfileImageGenerator(
            this.interactive.getPage(),
            this.interactive.getBoarUser(),
            this.interactive.getBadgeIDs(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.getFavoriteID(),
            this.interactive.isSkyblockGuild(),
            this.interactive.getProfileData()
        );
    }

    private MegaMenuGenerator makeCollectionGen() throws SQLException {
        this.updateCompendiumCollection();
        this.refreshFilterSort();

        this.interactive.setMaxPage(Math.max((this.interactive.getFilteredBoars().size()-1) / 15, 0));
        if (this.interactive.getPage() > this.interactive.getMaxPage()) {
            this.interactive.setPrevPage(this.interactive.getPage());
            this.interactive.setPage(this.interactive.getMaxPage());
        }

        return new CollectionImageGenerator(
            this.interactive.getPage(),
            this.interactive.getBoarUser(),
            this.interactive.getBadgeIDs(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.getFilteredBoars()
        );
    }

    private MegaMenuGenerator makeCompendiumGen() throws SQLException {
        this.updateCompendiumCollection();
        this.refreshFilterSort();

        if (this.interactive.getFilteredBoars().isEmpty()) {
            this.interactive.setCurView(MegaMenuView.COLLECTION);
            this.interactive.setInteractOpen(false);
            this.interactive.setSortOpen(false);
            this.interactive.setFilterOpen(false);

            this.interactive.setAcknowledgeOpen(true);
            this.interactive.setAcknowledgeImageGen(
                new OverlayImageGenerator(null, this.config.getStringConfig().getCompBlocked())
            );

            return this.makeCollectionGen();
        }

        this.interactive.setMaxPage(this.interactive.getFilteredBoars().size()-1);

        if (this.interactive.getBoarPage() != null) {
            this.interactive.setPrevPage(this.interactive.getPage());
            this.interactive.setPage(this.interactive.getFindBoarPage(this.interactive.getBoarPage()));
            this.interactive.setBoarPage(null);
        }

        if (this.interactive.getPage() > this.interactive.getMaxPage()) {
            this.interactive.setPrevPage(this.interactive.getPage());
            this.interactive.setPage(this.interactive.getMaxPage());
        }

        Iterator<Map.Entry<String, BoarInfo>> iterator = this.interactive.getFilteredBoars().entrySet().iterator();
        for (int i=0; i<this.interactive.getPage(); i++) {
            iterator.next();
        }

        this.interactive.setCurBoarEntry(iterator.next());
        this.interactive.setCurRarityKey(BoarUtil.findRarityKey(this.interactive.getCurBoarEntry().getKey()));

        return new CompendiumImageGenerator(
            this.interactive.getPage(),
            this.interactive.getBoarUser(),
            this.interactive.getBadgeIDs(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.getFavoriteID() != null &&
                this.interactive.getFavoriteID().equals(this.interactive.getCurBoarEntry().getKey()),
            this.interactive.getCurBoarEntry()
        );
    }

    private MegaMenuGenerator makeEditionsGen() {
        this.interactive.setMaxPage(
            Math.max((this.interactive.getCurBoarEntry().getValue().getEditions().size()-1) / 5, 0)
        );

        if (this.interactive.getPage() > this.interactive.getMaxPage()) {
            this.interactive.setPrevPage(this.interactive.getPage());
            this.interactive.setPage(this.interactive.getMaxPage());
        }

        return new EditionsImageGenerator(
            this.interactive.getPage(),
            this.interactive.getBoarUser(),
            this.interactive.getBadgeIDs(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.getCurBoarEntry()
        );
    }

    private MegaMenuGenerator makeStatsGen() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.setStatsData(this.interactive.getBoarUser().getStatsData(connection));
                this.interactive.getViewsToUpdateData().put(this.view, true);
            }
        }

        this.interactive.setMaxPage(7);
        if (this.interactive.getPage() > this.interactive.getMaxPage()) {
            this.interactive.setPrevPage(this.interactive.getPage());
            this.interactive.setPage(this.interactive.getMaxPage());
        }

        return new StatsImageGenerator(
            this.interactive.getPage(),
            this.interactive.getBoarUser(),
            this.interactive.getBadgeIDs(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.getStatsData()
        );
    }

    private void updateCompendiumCollection() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.setOwnedBoars(this.interactive.getBoarUser().getOwnedBoarInfo(connection));

                if (this.view == MegaMenuView.COMPENDIUM) {
                    this.interactive.setFavoriteID(this.interactive.getBoarUser().getFavoriteID(connection));
                    this.interactive.setNumTransmute(
                        this.interactive.getBoarUser().getPowerupAmount(connection, "transmute")
                    );
                    this.interactive.setNumClone(this.interactive.getBoarUser().getPowerupAmount(connection, "clone"));
                }

                BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());

                this.interactive.setFilterBits(interBoarUser.getFilterBits(connection));
                this.interactive.setSortVal(interBoarUser.getSortVal(connection));

                interBoarUser.decRefs();

                this.interactive.getViewsToUpdateData().put(MegaMenuView.COMPENDIUM, true);
                this.interactive.getViewsToUpdateData().put(MegaMenuView.COLLECTION, true);
            }
        }
    }

    private void refreshFilterSort() {
        this.interactive.setFilteredBoars(new LinkedHashMap<>());
        Map<String, RarityConfig> rarities = this.config.getRarityConfigs();
        int[] rarityBitShift = new int[] {1 + rarities.size()};

        List<String> newKeySet = new ArrayList<>(rarities.keySet());
        Collections.reverse(newKeySet);

        for (String rarityKey : newKeySet) {
            this.applyFilter(rarities.get(rarityKey), rarityKey, rarityBitShift);
        }

        LinkedHashMap<String, BoarInfo> sortedBoars = new LinkedHashMap<>();

        switch (this.interactive.getSortVal()) {
            case RARITY_A -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case AMOUNT_D -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.amountComparator().reversed()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case AMOUNT_A -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.amountComparator()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case RECENT_D -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.recentComparator().reversed()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case RECENT_A -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.recentComparator()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case NEWEST_D -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.newestComparator().reversed()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case NEWEST_A -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.newestComparator()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case ALPHA_D -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case ALPHA_A -> this.interactive.getFilteredBoars().entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));
        }

        if (!sortedBoars.isEmpty()) {
            this.interactive.setFilteredBoars(sortedBoars);
        }
    }

    private void applyFilter(RarityConfig rarity, String rarityKey, int[] rarityBitShift) {
        BoarInfo emptyBoarInfo = new BoarInfo(rarityKey);

        boolean ownedFilter = this.interactive.getFilterBits() % 2 == 1;
        boolean duplicateFilter = (this.interactive.getFilterBits() >> 1) % 2 == 1;
        boolean raritySelected = this.interactive.getFilterBits() > 3;

        boolean notRarityFilter = (this.interactive.getFilterBits() >> rarityBitShift[0]) % 2 == 0;
        rarityBitShift[0]--;
        if (raritySelected && notRarityFilter) {
            return;
        }

        for (String boarID : rarity.getBoars()) {
            // Owned filter
            if (ownedFilter && !this.interactive.getOwnedBoars().containsKey(boarID)) {
                continue;
            }

            // Duplicate filter
            boolean hasDuplicate = this.interactive.getOwnedBoars().containsKey(boarID) &&
                this.interactive.getOwnedBoars().get(boarID).getAmount() > 1;
            if (duplicateFilter && !hasDuplicate) {
                continue;
            }

            BoarItemConfig boar = this.config.getItemConfig().getBoars().get(boarID);
            boolean boarShouldHide = rarity.isHidden() || boar.isSecret();

            // No filter
            if (boar.isBlacklisted() || boarShouldHide && !this.interactive.getOwnedBoars().containsKey(boarID)) {
                continue;
            }

            this.interactive.getFilteredBoars().put(
                boarID, this.interactive.getOwnedBoars().getOrDefault(boarID, emptyBoarInfo)
            );
        }
    }
}
