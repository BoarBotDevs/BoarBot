package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.util.data.types.GithubData;
import dev.boarbot.util.json.JsonUtil;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public final class GithubDataUtil {
    public static GithubData getGithubData() throws IOException {
        BotConfig config = BoarBotApp.getBot().getConfig();

        String githubFilePath = config.getPathConfig().getDatabaseFolder() +
            config.getPathConfig().getGlobalDataFolder() + config.getPathConfig().getGithubFileName();
        Gson g = new Gson();

        try {
            String githubJson = JsonUtil.pathToJson(githubFilePath);
            return g.fromJson(githubJson, GithubData.class);
        } catch (FileNotFoundException exception) {
            if (!config.getPathConfig().getGithubFileName().isEmpty()) {
                GithubData newData = new GithubData();

                BufferedWriter writer = new BufferedWriter(new FileWriter(githubFilePath));
                writer.write(g.toJson(newData));
                writer.close();

                return newData;
            }

            throw exception;
        }
    }
}
