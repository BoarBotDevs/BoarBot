package dev.boarbot.util.interaction;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.concurrent.*;

public class InteractionUtil {
    private final static int SLEEP_TIME = 2000;
    private final static int CORE_POOL_SIZE = 14;
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

    public static void runWhenEdited(SlashCommandInteraction interaction, Runnable runnable, int waitVal) {
        if (waitVal <= 0) {
            runnable.run();
            return;
        }

        scheduler.schedule(
            () -> interaction.getHook().retrieveOriginal().queue(
                msg -> {
                    boolean notEdited = msg.getAttachments().isEmpty() && msg.getComponents().isEmpty() &&
                        msg.getContentRaw().isEmpty();

                    if (notEdited) {
                        runWhenEdited(interaction, runnable, waitVal-SLEEP_TIME);
                        return;
                    }

                    runnable.run();
                }
            ),
            SLEEP_TIME,
            TimeUnit.MILLISECONDS
        );
    }

    public static void shutdownScheduler() {
        scheduler.shutdown();
    }
}
