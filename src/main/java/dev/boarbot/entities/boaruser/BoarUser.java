package dev.boarbot.entities.boaruser;

import dev.boarbot.BoarBotApp;
import dev.boarbot.entities.boaruser.queries.*;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;

public class BoarUser {
    private User user;
    @Getter private final String userID;

    @Getter @Setter private boolean isFirstDaily = false;

    private final BaseQueries baseQuery;
    private final BoarQueries boarQuery;
    private final EventQueries eventQuery;
    private final GiftQueries giftQuery;
    private final MegaMenuQueries megaQuery;
    private final PowerupQueries powQuery;
    private final QuestQueries questQuery;

    @Getter boolean isSynchronized = false;
    @Getter private long lastRef;

    public BoarUser(User user) {
        this(user.getId());
        this.user = user;
    }

    public BoarUser(String userID) {
        this.userID = userID;
        this.baseQuery = new BaseQueries(this);
        this.boarQuery = new BoarQueries(this);
        this.eventQuery = new EventQueries(this);
        this.giftQuery = new GiftQueries(this);
        this.megaQuery = new MegaMenuQueries(this);
        this.powQuery = new PowerupQueries(this);
        this.questQuery = new QuestQueries(this);
    }

    public User getUser() {
        if (this.user == null) {
            this.user = BoarBotApp.getBot().getJDA().retrieveUserById(this.userID).complete();
        }
        return this.user;
    }

    public BaseQueries baseQuery() {
        return this.baseQuery;
    }

    public BoarQueries boarQuery() {
        return this.boarQuery;
    }

    public EventQueries eventQuery() {
        return this.eventQuery;
    }

    public GiftQueries giftQuery() {
        return this.giftQuery;
    }

    public MegaMenuQueries megaQuery() {
        return this.megaQuery;
    }

    public PowerupQueries powQuery() {
        return this.powQuery;
    }

    public QuestQueries questQuery() {
        return this.questQuery;
    }

    public synchronized void passSynchronizedAction(Synchronizable callingObject) {
        this.isSynchronized = true;
        callingObject.doSynchronizedAction(this);
        this.isSynchronized = false;
    }

    public void forceSynchronized() {
        if (!this.isSynchronized) {
            throw new IllegalStateException("Boar user must be synchronized to do this!");
        }
    }

    void updateLastRef() {
        this.lastRef = TimeUtil.getCurMilli();
        Log.debug(this.getClass(), "[U]%s".formatted(Log.getUserSuffix(this.user, this.userID)));

        try (Connection connection = DataUtil.getConnection()) {
            this.baseQuery.updateUser(connection, true);
        } catch (SQLException exception) {
            Log.error(this.getUser(), this.getClass(), "Failed to update user's data", exception);
        }
    }
}
