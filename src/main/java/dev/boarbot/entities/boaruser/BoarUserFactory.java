package dev.boarbot.entities.boaruser;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BoarUserFactory implements Configured {
    private final static Map<String, BoarUser> boarUsers = new HashMap<>();

    // Maximum amount of time for a reference to stay alive
    // Interactives stay alive for 5 minutes tops
    // An extra minute gives time for final queries to go through
    private final static int ALIVE_MILLIS = NUMS.getInteractiveHardStop() + 1000 * 60;

    public static BoarUser getBoarUser(User user) {
        return BoarUserFactory.getBoarUser(user, user.getId());
    }

    public static BoarUser getBoarUser(String userID) {
        return BoarUserFactory.getBoarUser(null, userID);
    }

    private static synchronized BoarUser getBoarUser(User user, String userID) {
        boolean shouldCreate = !BoarUserFactory.boarUsers.containsKey(userID);

        if (shouldCreate) {
            Log.debug(BoarUserFactory.class, "Creating BoarUser%s".formatted(Log.getUserSuffix(user, userID)));
        }

        if (shouldCreate && user == null) {
            BoarUserFactory.boarUsers.put(userID, new BoarUser(userID));
        } else if (shouldCreate) {
            BoarUserFactory.boarUsers.put(userID, new BoarUser(user));
        }

        BoarUser boarUser = BoarUserFactory.boarUsers.get(userID);
        boarUser.updateLastRef();

        if (shouldCreate) {
            CompletableFuture.runAsync(() -> tryRemoveBoarUser(boarUser, ALIVE_MILLIS));
        }

        return boarUser;
    }

    private static void tryRemoveBoarUser(BoarUser boarUser, long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException exception) {
            Log.error(BoarUserFactory.class, "Thread interrupted before completion", exception);
        }

        long curTime = TimeUtil.getCurMilli();

        if (boarUser.getLastRef() + ALIVE_MILLIS <= curTime) {
            removeBoarUser(boarUser.getUserID());
        } else {
            long newWaitTime = boarUser.getLastRef() + ALIVE_MILLIS - curTime;
            tryRemoveBoarUser(boarUser, newWaitTime);
        }
    }

    private static void removeBoarUser(String userID) {
        Log.debug(BoarUserFactory.class, "[GC]%s".formatted(Log.getUserSuffix(null, userID)));
        BoarUserFactory.boarUsers.remove(userID);
    }
}
