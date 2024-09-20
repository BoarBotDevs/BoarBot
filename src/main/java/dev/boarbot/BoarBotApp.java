package dev.boarbot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.BoarBot;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;

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

        bot = new BoarBot();
        bot.create();

        if (args.length != 0 && args[0].equals("deploy-commands")) {
            try {
                bot.getJDA().awaitReady();
            } catch (InterruptedException ignored) {}

            bot.deployCommands();
        }
    }

    public static void reset() {
        bot = null;
        main();
    }

    public static Dotenv getEnv() {
        return BoarBotApp.env;
    }
}
