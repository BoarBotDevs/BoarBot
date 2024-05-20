package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.util.data.types.PowerupData;
import dev.boarbot.util.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;

@Log4j2
public class PowerupsDataUtil extends DataUtil {
    private PowerupData data;

    public PowerupsDataUtil() {
        this(false);
    }

    public PowerupsDataUtil(boolean update) {
        this.filePath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder() +
            this.pathConfig.getPowerupDataFileName();
        this.data = refreshData(update);
    }

    @Override
    public PowerupData refreshData(boolean update) {
        createDatabaseFolders();

        this.data = new PowerupData();

        String dataJson = null;

        try {
            dataJson = JsonUtil.pathToJson(this.filePath);
        } catch (FileNotFoundException e) {
            log.info("Unable to find file at %s. Attempting to create.".formatted(this.filePath));
        }

        if (dataJson == null) {
            dataJson = createFile(this.filePath, this.data);
        }

        this.data = new Gson().fromJson(dataJson, PowerupData.class);
        return this.data;
    }

    @Override
    public PowerupData getData() {
        return this.data;
    }

    @Override
    public void saveData() throws IOException {
        saveData(this.filePath, this.data);
    }
}
