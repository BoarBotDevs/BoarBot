package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.logging.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PowerupQueries implements Configured {
    private final BoarUser boarUser;

    public PowerupQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    public void addPowerup(Connection connection, String powerupID, int amount) throws SQLException {
        this.addPowerup(connection, powerupID, amount, false);
    }

    public void addPowerup(Connection connection, String powerupID, int amount, boolean force) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();
        this.insertPowerupIfNotExist(connection, powerupID);

        String updateQuery = """
            UPDATE collected_powerups
            SET amount = LEAST(amount + ?, ?)
            WHERE user_id = ? AND powerup_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, amount);
            statement.setLong(2, powerupID.equals("transmute") && !force ? NUMS.getMaxTransmute() : Integer.MAX_VALUE);
            statement.setString(3, this.boarUser.getUserID());
            statement.setString(4, powerupID);
            statement.execute();
        }

        if (powerupID.equals("transmute")) {
            Log.info(this.boarUser.getUser(), this.getClass(), "Obtained +%,d %s".formatted(amount, powerupID));
        } else {
            Log.debug(this.boarUser.getUser(), this.getClass(), "Obtained +%,d %s".formatted(amount, powerupID));
        }
    }

    private void insertPowerupIfNotExist(Connection connection, String powerupID) throws SQLException {
        String query = """
            INSERT INTO collected_powerups (user_id, powerup_id)
            SELECT ?, ?
            WHERE NOT EXISTS (
                SELECT unique_id
                FROM collected_powerups
                WHERE user_id = ? AND powerup_id = ?
            );
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.setString(2, powerupID);
            statement.setString(3, this.boarUser.getUserID());
            statement.setString(4, powerupID);
            statement.execute();
        }
    }

    public int getPowerupAmount(Connection connection, String powerupID) throws SQLException {
        String query = """
            SELECT amount
            FROM collected_powerups
            WHERE user_id = ? AND powerup_id = ?;
        """;

        int amount = 0;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.setString(2, powerupID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    amount = results.getInt("amount");
                }
            }
        }

        return amount;
    }

    public void usePowerup(Connection connection, String powerupID, int amount) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE collected_powerups
            SET amount = amount - ?, amount_used = amount_used + ?
            WHERE user_id = ? AND powerup_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setInt(2, amount);
            statement.setString(3, this.boarUser.getUserID());
            statement.setString(4, powerupID);
            statement.executeUpdate();
        }
    }

    public void activateMiracles(Connection connection, int amount) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String updateQuery = """
            UPDATE users
            SET
                miracles_active = miracles_active + ?,
                highest_blessings = GREATEST(highest_blessings, ?),
                miracle_rolls = miracle_rolls + 1
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, amount);
            statement.setLong(2, this.boarUser.baseQuery().getBlessings(connection, amount));
            statement.setString(3, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        this.usePowerup(connection, "miracle", amount);
        Log.debug(this.boarUser.getUser(), this.getClass(), "Activated miracles");
    }

    public void useActiveMiracles(
        List<String> boarIDs, List<Integer> bucksGotten, Connection connection
    ) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        int totalBucks = bucksGotten.stream().reduce(0, Integer::sum);
        String bestRarity = null;

        for (String boarID : boarIDs) {
            String boarRarity = BoarUtil.findRarityKey(boarID);

            if (bestRarity == null) {
                bestRarity = boarRarity;
                continue;
            }

            bestRarity = BoarUtil.getHigherRarity(bestRarity, boarRarity);
        }

        String bestRarityQuery = """
            SELECT miracle_best_rarity
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(bestRarityQuery)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next() && results.getString("miracle_best_rarity") != null) {
                    bestRarity = BoarUtil.getHigherRarity(bestRarity, results.getString("miracle_best_rarity"));
                }
            }
        }

        String updateQuery = """
            UPDATE users
            SET miracles_active = 0, miracle_best_bucks = GREATEST(miracle_best_bucks, ?), miracle_best_rarity = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, totalBucks);
            statement.setString(2, bestRarity);
            statement.setString(3, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        Log.debug(this.boarUser.getUser(), this.getClass(), "Used activate miracles (if user had any active)");
    }
}
