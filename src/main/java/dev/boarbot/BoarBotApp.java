package dev.boarbot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.BoarBot;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.gift.BoarGiftInteractive;
import dev.boarbot.jobs.PowerupEventJob;
import dev.boarbot.migration.MigrationHandler;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.interaction.InteractionUtil;
import dev.boarbot.util.logging.Log;
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

        Runtime.getRuntime().addShutdownHook(new Thread(BoarBotApp::cleanup));

        if (args.length != 0 && args[0].equals("migrate")) {
            try {
                bot.getJDA().awaitReady();
            } catch (InterruptedException exception) {
                Log.error(BoarBotApp.class, "Main thread interrupted before bot was ready", exception);
                System.exit(-1);
            }

            MigrationHandler.doMigration();
        }

        if (args.length != 0 && args[0].equals("deploy-commands")) {
            try {
                bot.getJDA().awaitReady();
            } catch (InterruptedException exception) {
                Log.error(BoarBotApp.class, "Main thread interrupted before bot was ready", exception);
                System.exit(-1);
            }

            bot.deployCommands();
        }
    }

    private static void cleanup() {
        InteractionUtil.shutdownScheduler();
        PowerupEventJob.shutdownScheduler();

        for (ModalHandler modalHandler : BoarBotApp.getBot().getModalHandlers().values()) {
            modalHandler.shutdownScheduler();
        }

        for (Interactive interactive : BoarBotApp.getBot().getInteractives().values()) {
            interactive.shutdownScheduler();

            if (interactive instanceof BoarGiftInteractive) {
                ((BoarGiftInteractive) interactive).shutdownGiftScheduler();
            }
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
