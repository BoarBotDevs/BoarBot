package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.boar.BoarUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GiftQueries implements Configured {
    private final BoarUser boarUser;

    public GiftQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    public int getGiftHandicap(Connection connection) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        int handicap = 0;

        String query = """
            SELECT gift_handicap
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    handicap = results.getInt("gift_handicap");
                }
            }
        }

        return handicap;
    }

    public void updateGiftHandicap(Connection connection, long value) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        int MAX_HANDICAP_WEIGHT = 20;

        long handicapValue = value * -1;
        if (handicapValue < NUMS.getGiftMaxHandicap() * -1) {
            String handicapQuery = """
                SELECT gift_handicap
                FROM users
                WHERE user_id = ?;
            """;

            try (PreparedStatement statement = connection.prepareStatement(handicapQuery)) {
                statement.setString(1, this.boarUser.getUserID());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        handicapValue = results.getLong("gift_handicap");
                    }
                }
            }
        }

        String updateQuery = """
            UPDATE users
            SET
                gift_handicap = GREATEST(
                    ((gift_handicap * gift_handicap_weight) + ?) / (gift_handicap_weight + 1),
                    ?
                ),
                gift_handicap_weight = LEAST(gift_handicap_weight + 1, ?),
                gift_fastest = LEAST(gift_fastest, ?)
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setLong(1, handicapValue);
            statement.setLong(2, handicapValue);
            statement.setInt(3, MAX_HANDICAP_WEIGHT);
            statement.setLong(4, value);
            statement.setString(5, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public void openGift(Connection connection, int bucks, List<String> rarityKeys, boolean incOpen) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String bestRarity = null;

        String bestRarityQuery = """
            SELECT gift_best_rarity
            FROM users
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(bestRarityQuery)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    bestRarity = results.getString("gift_best_rarity");
                    for (String rarityKey : rarityKeys) {
                        bestRarity = BoarUtil.getHigherRarity(bestRarity, rarityKey);
                    }
                }
            }
        }

        String openGiftQuery = """
            UPDATE users
            SET
                gifts_opened = gifts_opened + ?,
                gift_best_bucks = GREATEST(gift_best_bucks, ?),
                gift_best_rarity = ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(openGiftQuery)) {
            statement.setInt(1, incOpen ? 1 : 0);
            statement.setInt(2, bucks);
            statement.setString(3, bestRarity);
            statement.setString(4, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }

    public void setAdventBits(Connection connection, int bits) throws SQLException {
        this.boarUser.baseQuery().addUser(connection);
        this.boarUser.forceSynchronized();

        String query = """
            UPDATE users
            SET advent_bits = ?, advent_year = year(utc_timestamp())
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bits);
            statement.setString(2, this.boarUser.getUserID());
            statement.executeUpdate();
        }
    }
}
