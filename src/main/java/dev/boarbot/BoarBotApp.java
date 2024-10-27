package dev.boarbot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.BoarBot;
import dev.boarbot.bot.EnvironmentType;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.gift.BoarGiftInteractive;
import dev.boarbot.jobs.PowerupEventJob;
import dev.boarbot.migration.MigrationHandler;
import dev.boarbot.util.deploy.DeployProduction;
import dev.boarbot.util.interaction.InteractionUtil;
import dev.boarbot.util.logging.Log;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * {@link BoarBotApp BoarBotApp.java}
 *
 * Creates the bot instance using CLI args.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
public class BoarBotApp {
    @Getter private static Bot bot;
    private static final Dotenv env = Paths.get(".env").toFile().exists()
        ? Dotenv.configure()
            .filename(".env")
            .load()
        : null;
    private static EnvironmentType environmentType;

    public static void main(String... args) {
        if (args.length > 0) {
            BoarBotApp.environmentType = switch (args[0]) {
                case "test" -> EnvironmentType.TEST;
                case "deploy" -> EnvironmentType.DEPLOY;
                case "prod" -> EnvironmentType.PROD;
                default -> EnvironmentType.DEV;
            };
        } else {
            BoarBotApp.environmentType = EnvironmentType.DEV;
        }

        if (BoarBotApp.environmentType == EnvironmentType.DEPLOY) {
            try {
                DeployProduction.deploy();
            } catch (IOException exception) {
                Log.error(BoarBotApp.class, "Failed to deploy production environment", exception);
                System.exit(-1);
            }

            return;
        }

        bot = new BoarBot();
        bot.create();

        try {
            bot.getJDA().awaitReady();
        } catch (InterruptedException exception) {
            Log.error(BoarBotApp.class, "Main thread interrupted before bot was ready", exception);
            System.exit(-1);
        }

        if (environmentType == EnvironmentType.PROD) {
            bot.deployCommands();
            MigrationHandler.doMigration();
        }

        if (environmentType != EnvironmentType.PROD && args.length > 1 && Boolean.parseBoolean(args[1])) {
            bot.deployCommands();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(BoarBotApp::cleanup));
    }

    private static void cleanup() {
        InteractionUtil.shutdownScheduler();
        PowerupEventJob.shutdownScheduler();

        for (Interactive interactive : BoarBotApp.getBot().getInteractives().values()) {
            if (interactive instanceof BoarGiftInteractive) {
                ((BoarGiftInteractive) interactive).shutdownGiftScheduler();
            }
        }
    }

    public static void reset() {
        bot = null;
        main();
    }

    public static String getEnv(String key) {
        if (BoarBotApp.env == null) {
            return System.getenv(key);
        }

        return BoarBotApp.env.get(key);
    }

    public static EnvironmentType getEnvironmentType() { return BoarBotApp.environmentType; }
}
