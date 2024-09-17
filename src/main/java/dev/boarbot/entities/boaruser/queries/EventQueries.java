package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.logging.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventQueries {
    private final BoarUser boarUser;

    public EventQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    public void applyPowEventWin(Connection connection, String powerupID, int powAmt, long time) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE users
            SET
                powerup_attempts = powerup_attempts + 1,
                powerup_wins = powerup_wins + 1,
                powerup_fastest_time = LEAST(powerup_fastest_time, ?)
            WHERE user_id = ?;
        """;

        this.boarUser.powQuery().addPowerup(connection, powerupID, powAmt);

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, time);
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public void applyPowEventFail(Connection connection) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE users
            SET powerup_attempts = powerup_attempts + 1
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public void addPerfectPowerup(Connection connection) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE users
            SET powerup_perfects = powerup_perfects + 1
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        Log.info(this.boarUser.getUser(), this.getClass(), "Perfect Powerup!");
    }

    public void addPrompt(Connection connection, String promptID, double placement) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();
        this.insertPromptIfNotExist(connection, promptID);

        String updateQuery = """
            UPDATE prompt_stats
            SET
                average_placement = ((average_placement * wins) + ?) / (wins + 1),
                wins = wins + 1
            WHERE user_id = ? AND prompt_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setDouble(1, placement);
            statement.setString(2, this.boarUser.getUserID());
            statement.setString(3, promptID);
            statement.executeUpdate();
        }
    }

    private void insertPromptIfNotExist(Connection connection, String promptID) throws SQLException {
        String query = """
            INSERT INTO prompt_stats (user_id, prompt_id)
            SELECT ?, ?
            WHERE NOT EXISTS (
                SELECT unique_id
                FROM prompt_stats
                WHERE user_id = ? AND prompt_id = ?
            );
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.setString(2, promptID);
            statement.setString(3, this.boarUser.getUserID());
            statement.setString(4, promptID);
            statement.execute();
        }
    }
}
