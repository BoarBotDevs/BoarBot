package dev.boarbot.entities.boaruser;

import dev.boarbot.BoarBotApp;
import dev.boarbot.entities.boaruser.queries.*;
import dev.boarbot.util.data.DataUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;

@Slf4j
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

    @Getter boolean isSynchronized = false;
    private int numRefs = 0;

    public BoarUser(User user) throws SQLException {
        this(user.getId());
        this.user = user;
    }

    public BoarUser(String userID) throws SQLException {
        this.userID = userID;
        this.baseQuery = new BaseQueries(this);
        this.boarQuery = new BoarQueries(this);
        this.eventQuery = new EventQueries(this);
        this.giftQuery = new GiftQueries(this);
        this.megaQuery = new MegaMenuQueries(this);
        this.powQuery = new PowerupQueries(this);
        this.incRefs();
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

    public synchronized void incRefs() throws SQLException {
        this.numRefs++;
        try (Connection connection = DataUtil.getConnection()) {
            this.baseQuery.updateUser(connection, true);
        }
    }

    public synchronized void decRefs() {
        if (--this.numRefs == 0) {
            BoarUserFactory.removeBoarUser(this.userID);
        }
    }
}
