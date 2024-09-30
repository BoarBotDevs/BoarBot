package dev.boarbot.util.data;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.items.BoarItemConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoarDataUtil implements Configured {
    public static int getTotalUniques(Connection connection) throws SQLException {
        int i=0;

        for (BoarItemConfig boar : BOARS.values()) {
            if (!boar.isBlacklisted()) {
                i++;
            }
        }

        return i;
    }

    public static int getTotalBoars(Connection connection) throws SQLException {
        String query = """
            SELECT COUNT(*)
            FROM collected_boars
            WHERE `exists` = true AND deleted = false;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        }

        return 0;
    }
}
