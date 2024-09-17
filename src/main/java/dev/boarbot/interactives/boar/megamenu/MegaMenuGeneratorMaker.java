package dev.boarbot.interactives.boar.megamenu;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.entities.boaruser.BoarInfo;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.QuestDataUtil;
import dev.boarbot.util.generators.OverlayImageGenerator;
import dev.boarbot.util.generators.megamenu.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

class MegaMenuGeneratorMaker implements Configured {
    private final MegaMenuInteractive interactive;
    private MegaMenuView view;

    public MegaMenuGeneratorMaker(MegaMenuInteractive interactive) {
        this.interactive = interactive;
        this.view = this.interactive.curView;
    }

    public MegaMenuGenerator make() throws SQLException {
        this.view = this.interactive.curView;

        return switch (this.view) {
            case MegaMenuView.PROFILE -> this.makeProfileGen();
            case MegaMenuView.COLLECTION -> this.makeCollectionGen();
            case MegaMenuView.COMPENDIUM -> this.makeCompendiumGen();
            case MegaMenuView.EDITIONS -> this.makeEditionsGen();
            case MegaMenuView.STATS -> this.makeStatsGen();
            case MegaMenuView.POWERUPS -> this.makePowerupsGen();
            case MegaMenuView.QUESTS -> this.makeQuestsGen();
            case MegaMenuView.BADGES -> this.makeBadgesGen();
        };
    }

    public MegaMenuGenerator makeProfileGen() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.profileData = this.interactive.getBoarUser().megaQuery().getProfileData(connection);
                this.interactive.getViewsToUpdateData().put(this.view, true);
            }
        }

        this.interactive.maxPage = 0;
        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        return new ProfileImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.favoriteID,
            this.interactive.isSkyblockGuild(),
            this.interactive.profileData
        );
    }

    private MegaMenuGenerator makeCollectionGen() throws SQLException {
        this.updateCompendiumCollection();
        this.refreshFilterSort();

        this.interactive.maxPage = Math.max((this.interactive.filteredBoars.size()-1) / 15, 0);
        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        return new CollectionImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.filteredBoars
        );
    }

    private MegaMenuGenerator makeCompendiumGen() throws SQLException {
        this.updateCompendiumCollection();
        this.refreshFilterSort();

        if (this.interactive.filteredBoars.isEmpty()) {
            this.interactive.curView = MegaMenuView.COLLECTION;

            this.interactive.acknowledgeOpen = true;
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getCompBlocked());

            return this.makeCollectionGen();
        }

        this.interactive.maxPage = this.interactive.filteredBoars.size()-1;

        if (this.interactive.boarPage != null) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.getFindBoarPage(this.interactive.boarPage);
            this.interactive.boarPage = null;
        }

        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        Iterator<Map.Entry<String, BoarInfo>> iterator = this.interactive.filteredBoars.entrySet().iterator();
        for (int i=0; i<this.interactive.page; i++) {
            iterator.next();
        }

        this.interactive.curBoarEntry = iterator.next();
        this.interactive.curRarityKey = BoarUtil.findRarityKey(this.interactive.curBoarEntry.getKey());

        return new CompendiumImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.favoriteID != null &&
                this.interactive.favoriteID.equals(this.interactive.curBoarEntry.getKey()),
            this.interactive.curBoarEntry
        );
    }

    private MegaMenuGenerator makeEditionsGen() {
        this.interactive.maxPage = Math.max((this.interactive.curBoarEntry.getValue().getEditions().size()-1) / 5, 0);

        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        return new EditionsImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.curBoarEntry
        );
    }

    private MegaMenuGenerator makeStatsGen() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.statsData = this.interactive.getBoarUser().megaQuery().getStatsData(connection);
                this.interactive.getViewsToUpdateData().put(this.view, true);
            }
        }

        this.interactive.maxPage = 7;
        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        return new StatsImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.statsData
        );
    }

    private MegaMenuGenerator makePowerupsGen() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.powData = this.interactive.getBoarUser().megaQuery().getPowerupsData(connection);
                this.interactive.getViewsToUpdateData().put(this.view, true);
            }
        }

        this.interactive.maxPage = 0;
        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        return new PowerupsImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.powData
        );
    }

    private MegaMenuGenerator makeQuestsGen() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.questData = this.interactive.getBoarUser().megaQuery().getQuestsData(connection);
                this.interactive.quests = QuestDataUtil.getQuests(connection);
                this.interactive.getViewsToUpdateData().put(this.view, true);
            }
        }

        this.interactive.maxPage = 0;
        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        return new QuestsImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate(),
            this.interactive.questData,
            this.interactive.quests
        );
    }

    private MegaMenuGenerator makeBadgesGen() throws SQLException {
        if (this.interactive.getBadges().isEmpty()) {
            if (this.interactive.prevView == null || this.interactive.prevView.equals(MegaMenuView.BADGES)) {
                this.interactive.curView = MegaMenuView.PROFILE;
            } else {
                this.interactive.curView = this.interactive.prevView;
            }

            this.interactive.acknowledgeOpen = true;
            this.interactive.acknowledgeImageGen = new OverlayImageGenerator(null, STRS.getBadgeBlocked());

            return this.make();
        }

        this.interactive.maxPage = this.interactive.getBadges().size()-1;
        if (this.interactive.page > this.interactive.maxPage) {
            this.interactive.prevPage = this.interactive.page;
            this.interactive.page = this.interactive.maxPage;
        }

        return new BadgesImageGenerator(
            this.interactive.page,
            this.interactive.getBoarUser(),
            this.interactive.getBadges(),
            this.interactive.getFirstJoinedDate()
        );
    }

    private void updateCompendiumCollection() throws SQLException {
        boolean notUpdated = this.interactive.getViewsToUpdateData().get(this.view) == null ||
            !this.interactive.getViewsToUpdateData().get(this.view);

        if (notUpdated) {
            try (Connection connection = DataUtil.getConnection()) {
                this.interactive.ownedBoars = this.interactive.getBoarUser().megaQuery().getOwnedBoarInfo(connection);

                if (this.view == MegaMenuView.COMPENDIUM) {
                    this.interactive.favoriteID = this.interactive.getBoarUser().megaQuery().getFavoriteID(connection);
                    this.interactive.numTransmute = this.interactive.getBoarUser().powQuery()
                        .getPowerupAmount(connection, "transmute");
                    this.interactive.numClone = this.interactive.getBoarUser().powQuery()
                        .getPowerupAmount(connection, "clone");
                }

                BoarUser interBoarUser = BoarUserFactory.getBoarUser(this.interactive.getUser());

                this.interactive.filterBits = interBoarUser.megaQuery().getFilterBits(connection);
                this.interactive.sortVal = interBoarUser.megaQuery().getSortVal(connection);

                this.interactive.getViewsToUpdateData().put(MegaMenuView.COMPENDIUM, true);
                this.interactive.getViewsToUpdateData().put(MegaMenuView.COLLECTION, true);
            }
        }
    }

    private void refreshFilterSort() {
        this.interactive.filteredBoars = new LinkedHashMap<>();
        int[] rarityBitShift = new int[] {1 + RARITIES.size()};

        List<String> newKeySet = new ArrayList<>(RARITIES.keySet());
        Collections.reverse(newKeySet);

        for (String rarityKey : newKeySet) {
            this.applyFilter(RARITIES.get(rarityKey), rarityKey, rarityBitShift);
        }

        LinkedHashMap<String, BoarInfo> sortedBoars = new LinkedHashMap<>();

        switch (this.interactive.sortVal) {
            case RARITY_A -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case AMOUNT_D -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.amountComparator().reversed()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case AMOUNT_A -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.amountComparator()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case RECENT_D -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.recentComparator().reversed()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case RECENT_A -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.recentComparator()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case NEWEST_D -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.newestComparator().reversed()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case NEWEST_A -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(BoarInfo.newestComparator()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case ALPHA_D -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));

            case ALPHA_A -> this.interactive.filteredBoars.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .forEachOrdered(entry -> sortedBoars.put(entry.getKey(), entry.getValue()));
        }

        if (!sortedBoars.isEmpty()) {
            this.interactive.filteredBoars = sortedBoars;
        }
    }

    private void applyFilter(RarityConfig rarity, String rarityKey, int[] rarityBitShift) {
        BoarInfo emptyBoarInfo = new BoarInfo(rarityKey);

        boolean ownedFilter = this.interactive.filterBits % 2 == 1;
        boolean duplicateFilter = (this.interactive.filterBits >> 1) % 2 == 1;
        boolean raritySelected = this.interactive.filterBits > 3;

        boolean notRarityFilter = (this.interactive.filterBits >> rarityBitShift[0]) % 2 == 0;
        rarityBitShift[0]--;
        if (raritySelected && notRarityFilter) {
            return;
        }

        for (String boarID : rarity.getBoars()) {
            // Owned filter
            if (ownedFilter && !this.interactive.ownedBoars.containsKey(boarID)) {
                continue;
            }

            // Duplicate filter
            boolean hasDuplicate = this.interactive.ownedBoars.containsKey(boarID) &&
                this.interactive.ownedBoars.get(boarID).getAmount() > 1;
            if (duplicateFilter && !hasDuplicate) {
                continue;
            }

            BoarItemConfig boar = BOARS.get(boarID);
            boolean boarShouldHide = rarity.isHidden() || boar.isSecret();

            // No filter
            if (boar.isBlacklisted() || boarShouldHide && !this.interactive.ownedBoars.containsKey(boarID)) {
                continue;
            }

            this.interactive.filteredBoars.put(
                boarID, this.interactive.ownedBoars.getOrDefault(boarID, emptyBoarInfo)
            );
        }
    }
}
