package dev.boarbot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.BoarBot;
import dev.boarbot.bot.BotType;
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

    public static void main(String... args) {
        if (args.length != 0 && args[0].equals("deploy-prod")) {
            System.out.println("Prod Deploy");
            return;
        }

        BoarBotApp.bot = new BoarBot();

        BotType botType = BotType.DEV;

        if (System.getProperty("environment") != null) {
            botType = switch(System.getProperty("environment").toLowerCase().charAt(0)) {
                case 'p' -> BotType.PROD;
                case 't' -> BotType.TEST;
                default -> BotType.DEV;
            };
        }

        BoarBotApp.bot.create(botType);

        if (args.length != 0 && args[0].equals("deploy-commands")) {
            BoarBotApp.bot.deployCommands();
        }
    }

    public static void reset() {
        BoarBotApp.bot = null;
        BoarBotApp.main();
    }
}
