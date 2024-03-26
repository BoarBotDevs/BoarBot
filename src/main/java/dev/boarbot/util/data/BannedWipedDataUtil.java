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

    private final String filePath;

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

    public Map<String, Long> refreshData(boolean update) {
        createGlobalFolder();

        this.data = new HashMap<>();

        Gson g = new Gson();
        Type mapType = new TypeToken<Map<String, Long>>() {}.getType();
        String dataJson = null;

        try {
            dataJson = JsonUtil.pathToJson(this.filePath);
        } catch (FileNotFoundException e) {
            log.info("Unable to find file at %s. Attempting to create.".formatted(this.filePath));
        }

        if (dataJson == null) {
            dataJson = g.toJson(this.data);

            try {
                saveData();
                log.info("Successfully created file %s.".formatted(this.filePath));
            } catch (IOException e) {
                log.error("Failed to create file %s.".formatted(this.filePath));
                System.exit(-1);
            }
        }

        this.data = g.fromJson(dataJson, mapType);
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
