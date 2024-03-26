package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.PathConfig;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Log4j2
public abstract class DataUtil {
    protected final BotConfig config = BoarBotApp.getBot().getConfig();
    protected final PathConfig pathConfig = this.config.getPathConfig();

    public abstract Object refreshData(boolean update);
    public abstract Object getData();
    public abstract void saveData() throws IOException;

    protected void saveData(String filePath, Object data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        Gson g = new Gson();

        writer.write(g.toJson(data));
        writer.close();
    }

    protected void createGlobalFolder() {
        String globalFolderPath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder();
        File globalFolder = new File(globalFolderPath);
        boolean madeGlobalFolder = globalFolder.mkdirs();

        if (!globalFolder.exists() && !madeGlobalFolder) {
            log.error("Something went wrong when creating global folder at %s!".formatted(globalFolderPath));
            System.exit(-1);
        }
    }
}
