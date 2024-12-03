package dev.boarbot.util.interaction;

import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class InteractionUtil {
    private final static int CORE_POOL_SIZE = 20;
    public final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

    public final static Map<String, Long> usersOnCooldown = new HashMap<>();

    private final static int RUN_SLEEP_TIME = 2000;
    public final static int COOLDOWN_SLEEP_TIME = 3000;

    static {
        scheduler.schedule(() -> {
            for (String user : usersOnCooldown.keySet()) {
                if (usersOnCooldown.get(user) < TimeUtil.getCurMilli() - COOLDOWN_SLEEP_TIME) {
                    usersOnCooldown.remove(user);
                }
            }
        }, COOLDOWN_SLEEP_TIME, TimeUnit.MILLISECONDS);
    }

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
        if (usersOnCooldown.containsKey(user.getId())) {
            return true;
        }

        usersOnCooldown.put(user.getId(), TimeUtil.getCurMilli());
        scheduler.schedule(() -> usersOnCooldown.remove(user.getId()), COOLDOWN_SLEEP_TIME, TimeUnit.MILLISECONDS);

        return false;
    }

    public static void shutdownScheduler() {
        scheduler.shutdown();
    }
}
