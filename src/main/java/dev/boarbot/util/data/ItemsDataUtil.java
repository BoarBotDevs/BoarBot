package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.collectibles.CollectedPowerup;
import dev.boarbot.entities.boaruser.stats.GeneralStats;
import dev.boarbot.util.data.types.BuySellData;
import dev.boarbot.util.data.types.ItemData;
import dev.boarbot.util.data.types.ItemsData;
import dev.boarbot.util.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

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

    public ItemsData refreshData(boolean update) {
        createGlobalFolder();

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

        for (String powerupID : powerupData.keySet()) {
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

                CollectedPowerup curPowerup = boarUser.getData().getItemCollection().getPowerups().get(powerupID);
                GeneralStats genStats = boarUser.getData().getStats().getGeneral();

                curPowerup.setNumTotal(
                    curPowerup.getNumTotal() + buyOrder.getFilledAmount() - buyOrder.getClaimedAmount()
                );

                genStats.setBoarScore(
                    genStats.getBoarScore() + (long) (buyOrder.getNum() - buyOrder.getFilledAmount()) *
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

                CollectedPowerup curPowerup = boarUser.getData().getItemCollection().getPowerups().get(powerupID);
                GeneralStats genStats = boarUser.getData().getStats().getGeneral();

                curPowerup.setNumTotal(
                    curPowerup.getNumTotal() + sellOrder.getNum() - sellOrder.getFilledAmount()
                );

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

        try {
            saveData();
        } catch (IOException exception) {
            log.error("Failed to update file %s.".formatted(filePath), exception);
            System.exit(-1);
        }
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
