package dev.boarbot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.resource.ResourceUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigUpdater implements Configured {
    private static final Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void setMaintenance(boolean status) throws IOException {
        if (CONFIG.getMainConfig().isMaintenanceMode() == status) {
            return;
        }

        CONFIG.getMainConfig().setMaintenanceMode(status);
        String jsonStr = g.toJson(CONFIG.getMainConfig());

        updateFile(ConfigLoader.mainPath, jsonStr);

        if (CONFIG.getMainConfig().isMaintenanceMode()) {
            for (Interactive interactive : BoarBotApp.getBot().getInteractives().values()) {
                interactive.stop(StopType.EXPIRED);
            }
        }
    }

    public static void clearTrophyGuessStr() throws IOException {
        STRS.setTrophyGuessStr(null);
        String jsonStr = g.toJson(STRS);

        updateFile(ConfigLoader.strsPath, jsonStr);
    }

    private static void updateFile(String pathStr, String jsonStr) throws IOException {
        Path path = ResourceUtil.resourcepackDir.resolve(pathStr);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        Files.write(path, List.of(jsonStr));
    }
}
