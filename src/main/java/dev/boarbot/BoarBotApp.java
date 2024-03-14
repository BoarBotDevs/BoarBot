package dev.boarbot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.BoarBot;

/**
 * {@link BoarBotApp BoarBotApp.java}
 *
 * Creates the bot instance using CLI args.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
public class BoarBotApp {
    private static Bot bot;

    public static void main(String[] args) {
        bot = new BoarBot();

        if (args.length != 0 && args[0].equals("deploy-prod")) {
            System.out.println("Prod Deploy");
            return;
        }

        bot.create();

        if (args.length != 0 && args[0].equals("deploy-commands")) {
            System.out.println("Command Deploy");
        }
    }

    public static Bot getBot() {
        return bot;
    }
}
