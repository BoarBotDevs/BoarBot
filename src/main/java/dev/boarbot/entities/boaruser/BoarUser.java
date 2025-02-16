package dev.boarbot.entities.boaruser;

import dev.boarbot.BoarBotApp;
import dev.boarbot.entities.boaruser.queries.*;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

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
        return this.getUser(false);
    }

    public User getUser(boolean isChecking) {
        if (this.user != null) {
            return user;
        }

        if (!isChecking) {
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

    public synchronized void passSynchronizedAction(Runnable runnable) {
        this.isSynchronized = true;
        runnable.run();
        this.isSynchronized = false;
    }

    public void forceSynchronized() {
        if (!this.isSynchronized) {
            throw new IllegalStateException("Boar user must be synchronized to do this!");
        }
    }
}
