package dev.boarbot.util.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.util.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class BannedWipedDataUtil extends DataUtil {
    private Map<String, Long> data;

    public BannedWipedDataUtil(boolean isBannedFile) {
        this(isBannedFile, false);
    }

    public BannedWipedDataUtil(boolean isBannedFile, boolean update) {
        String globalFolder = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder();

        if (isBannedFile) {
            this.filePath = globalFolder + this.pathConfig.getBannedUsersFileName();
        } else {
            this.filePath = globalFolder + this.pathConfig.getWipeUsersFileName();
        }

        this.data = refreshData(update);
    }

    @Override
    public Map<String, Long> refreshData(boolean update) {
        createDatabaseFolders();

        this.data = new HashMap<>();

        Type mapType = new TypeToken<Map<String, Long>>() {}.getType();
        String dataJson = null;

        try {
            dataJson = JsonUtil.pathToJson(this.filePath);
        } catch (FileNotFoundException e) {
            log.info("Unable to find file at %s. Attempting to create.".formatted(this.filePath));
        }

        if (dataJson == null) {
            dataJson = createFile(this.filePath, this.data);
        }

        this.data = new Gson().fromJson(dataJson, mapType);
        return this.data;
    }

    @Override
    public Map<String, Long> getData() {
        return this.data;
    }

    @Override
    public void saveData() throws IOException {
        saveData(this.filePath, this.data);
    }
}
