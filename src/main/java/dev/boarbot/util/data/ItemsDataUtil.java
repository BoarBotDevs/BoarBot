package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.stats.GeneralStats;
import dev.boarbot.util.data.types.BuySellData;
import dev.boarbot.util.data.types.ItemData;
import dev.boarbot.util.data.types.ItemsData;
import dev.boarbot.util.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Log4j2
public class ItemsDataUtil extends DataUtil {
    private ItemsData data;

    public ItemsDataUtil() {
        this(false);
    }

    public ItemsDataUtil(boolean update) {
        this.filePath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder() +
            this.pathConfig.getItemDataFileName();
        this.data = refreshData(update);
    }

    @Override
    public ItemsData refreshData(boolean update) {
        createDatabaseFolders();

        this.data = new ItemsData();

        String dataJson = null;

        try {
            dataJson = JsonUtil.pathToJson(this.filePath);
        } catch (FileNotFoundException e) {
            log.info("Unable to find file at %s. Attempting to create.".formatted(this.filePath));
        }

        if (dataJson == null) {
            for (String powerupID : this.config.getItemConfig().getPowerups().keySet()) {
                this.data.getPowerups().put(powerupID, new ItemData());
            }

            dataJson = createFile(this.filePath, this.data);
        }

        this.data = new Gson().fromJson(dataJson, ItemsData.class);

        if (update) {
            updateData();
        }

        return this.data;
    }

    private void updateData() {
        Map<String, IndivItemConfig> powerupConfig = this.config.getItemConfig().getPowerups();
        Map<String, ItemData> powerupData = this.data.getPowerups();

        for (String powerupID : powerupConfig.keySet()) {
            powerupData.putIfAbsent(powerupID, new ItemData());
        }

        for (String powerupID : new HashSet<>(powerupData.keySet())) {
            if (powerupConfig.containsKey(powerupID)) {
                continue;
            }

            ItemData powItemData = powerupData.get(powerupID);

            for (BuySellData buyOrder : powItemData.getBuyers()) {
                BoarUser boarUser;

                try {
                    boarUser = new BoarUser(buyOrder.getUserID());
                } catch (IOException exception) {
                    log.error("Failed to get user (%s) data.".formatted(buyOrder.getUserID()), exception);
                    continue;
                }

                GeneralStats genStats = boarUser.getData().getStats().getGeneral();

                genStats.setBoarScore(
                    genStats.getBoarScore() + (long) (buyOrder.getNum() - buyOrder.getClaimedAmount()) *
                        buyOrder.getPrice()
                );

                try {
                    boarUser.updateUserData();
                } catch (IOException exception) {
                    log.error("Failed to update user (%s) data.".formatted(buyOrder.getUserID()), exception);
                }
            }

            for (BuySellData sellOrder : powItemData.getSellers()) {
                BoarUser boarUser;

                try {
                    boarUser = new BoarUser(sellOrder.getUserID());
                } catch (IOException exception) {
                    log.error("Failed to get user (%s) data.".formatted(sellOrder.getUserID()), exception);
                    continue;
                }

                GeneralStats genStats = boarUser.getData().getStats().getGeneral();

                genStats.setBoarScore(
                    genStats.getBoarScore() + (long) (sellOrder.getFilledAmount() - sellOrder.getClaimedAmount()) *
                        sellOrder.getPrice()
                );

                try {
                    boarUser.updateUserData();
                } catch (IOException exception) {
                    log.error("Failed to update user (%s) data.".formatted(sellOrder.getUserID()), exception);
                }
            }

            powerupData.remove(powerupID);
        }

        orderGlobalBoars();

        try {
            saveData();
        } catch (IOException exception) {
            log.error("Failed to update file %s.".formatted(filePath), exception);
            System.exit(-1);
        }
    }

    private void orderGlobalBoars() {
        String[] boarIDs = this.data.getBoars().keySet().toArray(new String[0]);
        RarityConfig[] rarities = this.config.getRarityConfigs().values().toArray(new RarityConfig[0]);

        for (int i=rarities.length-1; i>0; i--) {
            Set<String> orderedBoars = new HashSet<>();
            String[] boarsOfRarity = rarities[i].boars;

            for (int j=0; j<boarIDs.length; j++) {
                String curBoarID = boarIDs[j];
                ItemData curBoarData = this.data.getBoars().get(curBoarID);

                if (!Arrays.asList(boarsOfRarity).contains(curBoarID) || orderedBoars.contains(curBoarID)) {
                    continue;
                }

                this.data.getBoars().remove(curBoarID);
                this.data.getBoars().put(curBoarID, curBoarData);

                orderedBoars.add(curBoarID);
                j--;
            }
        }
    }

    public static synchronized void updateGlobalBoarData(
        List<String> boarIDs, List<String> boarRarityKeys, List<Integer> boarEditions, List<Integer> firstEditions
    ) throws IOException {
        Map<String, RarityConfig> rarityConfigMap = BoarBotApp.getBot().getConfig().getRarityConfigs();
        ItemsDataUtil itemsDataUtil = new ItemsDataUtil();
        ItemsData itemsData = itemsDataUtil.getData();

        for (int i=0; i<boarIDs.size(); i++) {
            String boarID = boarIDs.get(i);
            RarityConfig boarRarity = rarityConfigMap.get(boarRarityKeys.get(i));
            boolean givesSpecial = boarRarity.givesSpecial;

            if (!itemsData.getBoars().containsKey(boarID)) {
                itemsData.getBoars().put(boarID, new ItemData());
                itemsData.getBoars().get(boarID).setCurEdition(0);

                int lastBuySell = boarRarity.baseScore == 1 ? 4 : boarRarity.baseScore;
                itemsData.getBoars().get(boarID).setLastBestBuyPrice(lastBuySell);
                itemsData.getBoars().get(boarID).setLastBestSellPrice(lastBuySell);

                if (givesSpecial) {
                    if (!itemsData.getBoars().containsKey("bacteria")) {
                        itemsData.getBoars().put("bacteria", new ItemData());
                        itemsData.getBoars().get("bacteria").setCurEdition(0);
                    }

                    int firstEdition = itemsData.getBoars().get("bacteria").getCurEdition() + 1;
                    itemsData.getBoars().get("bacteria").setCurEdition(firstEdition);
                    firstEditions.add(firstEdition);
                }
            }

            int boarEdition = itemsData.getBoars().get(boarID).getCurEdition() + 1;
            itemsData.getBoars().get(boarID).setCurEdition(boarEdition);
            boarEditions.add(boarEdition);
        }

        itemsDataUtil.orderGlobalBoars();
        itemsDataUtil.saveData();
    }

    @Override
    public ItemsData getData() {
        return this.data;
    }

    @Override
    public void saveData() throws IOException {
        saveData(this.filePath, this.data);
    }
}
