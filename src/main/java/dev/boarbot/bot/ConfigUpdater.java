package dev.boarbot.bot;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.resource.ResourceUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigUpdater implements Configured {
    public static void toggleMaintenance() throws IOException {
        CONFIG.getMainConfig().setMaintenanceMode(!CONFIG.getMainConfig().isMaintenanceMode());
        String jsonStr = new Gson().toJson(CONFIG.getMainConfig());

        updateFile(ConfigLoader.mainPath, jsonStr);

        if (CONFIG.getMainConfig().isMaintenanceMode()) {
            for (Interactive interactive : BoarBotApp.getBot().getInteractives().values()) {
                interactive.stop(StopType.EXPIRED);
            }
        }
    }

    private static void updateFile(String pathStr, String jsonStr) throws IOException {
        Path path = ResourceUtil.resourcepackDir.resolve(pathStr);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        List<String> lines = List.of(jsonStr);
        Files.write(path, lines);
    }
}
