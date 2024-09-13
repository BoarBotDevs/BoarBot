package dev.boarbot.entities.boaruser;

import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BoarUserFactory {
    private final static Map<String, BoarUser> boarUsers = new HashMap<>();

    public static BoarUser getBoarUser(User user) throws SQLException {
        return BoarUserFactory.getBoarUser(user, user.getId());
    }

    public static BoarUser getBoarUser(String userID) throws SQLException {
        return BoarUserFactory.getBoarUser(null, userID);
    }

    private static synchronized BoarUser getBoarUser(User user, String userID) throws SQLException {
        if (BoarUserFactory.boarUsers.containsKey(userID)) {
            BoarUser boarUser = BoarUserFactory.boarUsers.get(userID);
            boarUser.incRefs();
            return boarUser;
        }

        if (user == null) {
            BoarUserFactory.boarUsers.put(userID, new BoarUser(userID));
        } else {
            BoarUserFactory.boarUsers.put(userID, new BoarUser(user));
        }

        return BoarUserFactory.boarUsers.get(userID);
    }

    static void removeBoarUser(String userID) {
        BoarUserFactory.boarUsers.remove(userID);
    }
}
