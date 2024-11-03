package dev.boarbot.util.deploy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.MainConfig;
import dev.boarbot.util.resource.ResourceUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DeployProduction {
    private static final Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String predeployScript = "./deployment/predeploy.sh";
    private static final String deployScript = "./deployment/deploy.sh";

    public static void deploy() throws IOException {
        Runtime.getRuntime().exec(new String[] {predeployScript});
        writeProdEnv();
        writeProdResourcepack();
        Runtime.getRuntime().exec(new String[] {deployScript});
    }

    private static void writeProdEnv() throws IOException {
        Path prodEnvPath = Paths.get(".env_prod");

        List<String> lines = new ArrayList<>();
        lines.add("TOKEN=\"%s\"".formatted(BoarBotApp.getEnv("PROD_TOKEN")));
        lines.add("DB_PASS=\"%s\"".formatted(BoarBotApp.getEnv("PROD_DB_PASS")));

        Files.write(prodEnvPath, lines);
    }

    private static void writeProdResourcepack() throws IOException {
        Path mainConfigPath = Paths.get("config/config.json");
        Path prodMainConfigPath = Paths.get("resourcepack_prod/config/config.json");

        if (!Files.exists(prodMainConfigPath.getParent())) {
            Files.createDirectories(prodMainConfigPath.getParent());
        }

        MainConfig mainConfig = new MainConfig();

        if (!Files.exists(prodMainConfigPath)) {
            Files.createFile(prodMainConfigPath);
        } else {
            try (InputStream stream = ResourceUtil.getResourceStream(mainConfigPath.toString())) {
                InputStreamReader reader = new InputStreamReader(stream);
                mainConfig = new Gson().fromJson(reader, MainConfig.class);
            }
        }

        mainConfig.setDevGuild(BoarBotApp.getEnv("PROD_DEV_GUILD_ID"));
        mainConfig.setLogChannel(BoarBotApp.getEnv("PROD_LOG_CHANNEL"));
        mainConfig.setReportsChannel(BoarBotApp.getEnv("PROD_REPORTS_CHANNEL"));
        mainConfig.setPingChannel(BoarBotApp.getEnv("PROD_DEFAULT_CHANNEL"));
        mainConfig.setSpookChannel(BoarBotApp.getEnv("PROD_SPOOK_CHANNEL"));
        mainConfig.setUnlimitedBoars(false);

        Files.write(prodMainConfigPath, List.of(g.toJson(mainConfig)));
    }
}
