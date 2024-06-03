package dev.boarbot.entities.boaruser;

import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class BoarUserFactory {
    private final static Map<String, BoarUser> boarUsers = new HashMap<>();

    public static BoarUser getBoarUser(User user) {
        return BoarUserFactory.getBoarUser(user, user.getId());
    }

    public static BoarUser getBoarUser(String userID) {
        return BoarUserFactory.getBoarUser(null, userID);
    }

    private static synchronized BoarUser getBoarUser(User user, String userID) {
        if (BoarUserFactory.boarUsers.containsKey(userID)) {
            BoarUser boarUser = BoarUserFactory.boarUsers.get(userID);
            boarUser.incRefs();
            return boarUser;
        }

        BoarUserFactory.boarUsers.put(user.getId(), new BoarUser(user));
        return BoarUserFactory.boarUsers.get(userID);
    }

    static void removeBoarUser(String userID) {
        BoarUserFactory.boarUsers.remove(userID);
    }
}
