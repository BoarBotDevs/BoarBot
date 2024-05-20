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
    protected String filePath;

    protected final BotConfig config = BoarBotApp.getBot().getConfig();
    protected final PathConfig pathConfig = this.config.getPathConfig();

    public abstract Object refreshData(boolean update);
    public abstract Object getData();
    public abstract void saveData() throws IOException;

    protected String createFile(String filePath, Object data) {
        try {
            saveData(filePath, data);
            log.info("Successfully created file %s.".formatted(filePath));
        } catch (IOException exception) {
            log.error("Failed to create file %s.".formatted(filePath), exception);
            System.exit(-1);
        }

        return new Gson().toJson(data);
    }

    protected void saveData(String filePath, Object data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        Gson g = new Gson();

        writer.write(g.toJson(data));
        writer.close();
    }

    protected void createDatabaseFolders() {
        String globalFolderPath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder();
        File globalFolder = new File(globalFolderPath);
        boolean madeGlobalFolder = globalFolder.mkdirs();

        String guildFolderPath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGuildDataFolder();
        File guildFolder = new File(guildFolderPath);
        boolean madeGuildFolder = guildFolder.mkdirs();

        String usersFolderPath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getUserDataFolder();
        File usersFolder = new File(usersFolderPath);
        boolean madeUsersFolder = usersFolder.mkdirs();

        if (!globalFolder.exists() && !madeGlobalFolder) {
            log.error("Something went wrong when creating global folder at %s!".formatted(globalFolderPath));
            System.exit(-1);
        }

        if (!guildFolder.exists() && !madeGuildFolder) {
            log.error("Something went wrong when creating guilds folder at %s!".formatted(guildFolderPath));
            System.exit(-1);
        }

        if (!usersFolder.exists() && !madeUsersFolder) {
            log.error("Something went wrong when creating users folder at %s!".formatted(usersFolderPath));
            System.exit(-1);
        }
    }

    public static void updateAllData() {
        new ItemsDataUtil(true);
        new BoardsDataUtil(true);
        new BannedWipedDataUtil(true, true);
        new BannedWipedDataUtil(false, true);
        new PowerupsDataUtil(true);
        new QuestsDataUtil(true);
    }
}
