package dev.boarbot.entities.boaruser;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.entities.User;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoarUserFactory implements Configured {
    private final static Map<String, WeakReference<BoarUser>> boarUserPool = new ConcurrentHashMap<>();
    private final static ReferenceQueue<BoarUser> refQueue = new ReferenceQueue<>();

    public static int getNumBoarUsers() {
        return boarUserPool.size();
    }

    public static BoarUser getBoarUser(User user) {
        return BoarUserFactory.getBoarUser(user, user.getId());
    }

    public static BoarUser getBoarUser(String userID) {
        return BoarUserFactory.getBoarUser(null, userID);
    }

    private static synchronized BoarUser getBoarUser(User user, String userID) {
        cleanUp();

        WeakReference<BoarUser> boarUserRef = boarUserPool.get(userID);
        BoarUser boarUser = boarUserRef == null ? null : boarUserRef.get();
        boolean isNewBoarUser = boarUser == null;

        if (isNewBoarUser && user != null) {
            boarUser = new BoarUser(user);
        } else if (isNewBoarUser) {
            boarUser = new BoarUser(userID);
        }

        try (Connection connection = DataUtil.getConnection()) {
            boarUser.baseQuery().updateUser(connection, true);
        } catch (SQLException exception) {
            Log.error(
                BoarUserFactory.class,
                "Failed to update user's data%s".formatted(Log.getUserSuffix(user, userID)),
                exception
            );
        }

        if (isNewBoarUser) {
            boarUserPool.put(userID, new WeakReference<>(boarUser, refQueue));
        }

        return boarUser;
    }

    private static void cleanUp() {
        WeakReference<? extends BoarUser> boarUserRef = (WeakReference<? extends BoarUser>) refQueue.poll();
        while (boarUserRef != null) {
            boarUserPool.values().remove(boarUserRef);
            boarUserRef = (WeakReference<? extends BoarUser>) refQueue.poll();
        }
    }
}
