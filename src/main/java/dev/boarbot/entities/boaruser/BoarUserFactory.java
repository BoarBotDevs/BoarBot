package dev.boarbot.entities.boaruser;

import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BoarUserFactory {
    private final static Map<String, BoarUser> boarUsers = new HashMap<>();

    public static BoarUser getBoarUser(User user, boolean create) throws IOException {
        return BoarUserFactory.getBoarUser(user, user.getId(), create);
    }

    public static BoarUser getBoarUser(String userID) throws IOException {
        return BoarUserFactory.getBoarUser(null, userID, false);
    }

    private static BoarUser getBoarUser(User user, String userID, boolean create) throws IOException {
        if (BoarUserFactory.boarUsers.containsKey(userID)) {
            BoarUser boarUser = BoarUserFactory.boarUsers.get(userID);
            boarUser.incRefs();
            return boarUser;
        }

        BoarUser boarUser = new BoarUser(user, userID, create);
        BoarUserFactory.boarUsers.put(user.getId(), boarUser);
        return boarUser;
    }

    static void removeBoarUser(String userID) {
        BoarUserFactory.boarUsers.remove(userID);
    }
}
