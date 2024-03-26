package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.bot.config.items.IndivItemConfig;
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

        Gson g = new Gson();
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

        this.data = g.fromJson(dataJson, ItemsData.class);

        if (update) {
            updateData();
        }

        return this.data;
    }

    private void updateData() {
        Map<String, IndivItemConfig> powerupConfig = this.config.getItemConfig().getPowerups();
        Map<String, ItemData> powerupData = this.data.getPowerups();

        for (String powerupID : powerupConfig.keySet()) {
            powerupData.computeIfAbsent(powerupID, key -> new ItemData());
        }

        for (String powerupID : powerupData.keySet()) {
            if (powerupConfig.containsKey(powerupID)) {
                continue;
            }

            ItemData powItemData = powerupData.get(powerupID);

            for (BuySellData buyOrder : powItemData.getBuyers()) {

            }
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
