package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.util.data.types.GuildData;
import dev.boarbot.util.json.JsonUtil;

import java.io.*;

public final class GuildDataUtil {
    public static GuildData getGuildData(String guildID) throws IOException {
        return getGuildData(guildID, false);
    }

    public static GuildData getGuildData(String guildID, boolean create) throws IOException {
        BotConfig config = BoarBotApp.getBot().getConfig();

        String guildDataPath = config.getPathConfig().getDatabaseFolder() +
            config.getPathConfig().getGuildDataFolder() + guildID + ".json";
        Gson g = new Gson();

        try {
            String guildJson = JsonUtil.pathToJson(guildDataPath);
            return g.fromJson(guildJson, GuildData.class);
        } catch (FileNotFoundException exception) {
            if (create) {
                GuildData newData = new GuildData();

                BufferedWriter writer = new BufferedWriter(new FileWriter(guildDataPath));
                writer.write(g.toJson(newData));
                writer.close();

                return newData;
            }

            throw exception;
        }
    }

    public void removeGuildFile(String guildDataPath) throws IOException {
        File guildDataFile = new File(guildDataPath);
        boolean success = guildDataFile.delete();

        if (!success) {
            throw new IOException("Failed to delete guild file.");
        }
    }
}
