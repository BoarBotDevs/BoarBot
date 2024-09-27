package dev.boarbot.util.interaction;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class InteractionUtil {
    private final static int CORE_POOL_SIZE = 14;
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

    private final static Set<String> usersOnCooldown = new HashSet<>();

    private final static int RUN_SLEEP_TIME = 2000;
    private final static int COOLDOWN_SLEEP_TIME = 3000;

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
                        runWhenEdited(interaction, runnable, waitVal-RUN_SLEEP_TIME);
                        return;
                    }

                    runnable.run();
                }
            ),
            RUN_SLEEP_TIME,
            TimeUnit.MILLISECONDS
        );
    }

    public synchronized static boolean isOnCooldown(User user) {
        if (usersOnCooldown.contains(user.getId())) {
            return true;
        }

        usersOnCooldown.add(user.getId());
        scheduler.schedule(() -> usersOnCooldown.remove(user.getId()), COOLDOWN_SLEEP_TIME, TimeUnit.MILLISECONDS);

        return false;
    }

    public static void shutdownScheduler() {
        scheduler.shutdown();
    }
}
