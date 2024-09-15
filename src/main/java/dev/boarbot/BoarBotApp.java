package dev.boarbot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.BoarBot;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;

import java.io.InputStream;
import java.net.URL;

/**
 * {@link BoarBotApp BoarBotApp.java}
 *
 * Creates the bot instance using CLI args.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
public class BoarBotApp {
    @Getter private static Bot bot;
    private static final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();

    public static void main(String... args) {
        if (args.length != 0 && args[0].equals("deploy-prod")) {
            System.out.println("Prod Deploy");
            return;
        }

        BoarBotApp.bot = new BoarBot();
        BoarBotApp.bot.create();

        if (args.length != 0 && args[0].equals("deploy-commands")) {
            BoarBotApp.bot.deployCommands();
        }
    }

    public static void reset() {
        BoarBotApp.bot = null;
        BoarBotApp.main();
    }

    public static Dotenv getEnv() {
        return BoarBotApp.env;
    }

    public static URL getResource(String path) {
        return BoarBotApp.class.getClassLoader().getResource(path);
    }

    public static InputStream getResourceStream(String path) {
        return BoarBotApp.class.getClassLoader().getResourceAsStream(path);
    }
}
