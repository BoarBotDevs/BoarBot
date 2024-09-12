package dev.boarbot.bot;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BadgeItemConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
class DatabaseLoader implements Configured {
    public static void loadIntoDatabase(String databaseType) {
        try (Connection connection = DataUtil.getConnection()) {
            if (databaseType.equals("rarities")) {
                String resetQuery = """
                    DELETE FROM rarities_info
                    WHERE rarity_id = 'all_done';
                """;

                try (PreparedStatement statement = connection.prepareStatement(resetQuery)) {
                    statement.executeUpdate();
                }
            }

            if (databaseType.equals("badges")) {
                fixBadges(connection);
            } else {
                fixInfoTables(databaseType, connection);
            }

            if (databaseType.equals("boars")) {
                String resetQuery = """
                    INSERT INTO rarities_info (rarity_id, prior_rarity_id, base_bucks, researcher_need)
                    VALUES ('all_done', null, 0, 0)
                """;

                try (PreparedStatement statement = connection.prepareStatement(resetQuery)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            log.error("Something went wrong when loading config data into database.", exception);
            System.exit(-1);
        }
    }

    private static void fixBadges(Connection connection) throws SQLException {
        String removeAllUpdate = """
            UPDATE collected_badges
            SET `exists` = false;
        """;

        String restoreBadgeUpdate = """
            UPDATE collected_badges
            SET `exists` = true
            WHERE badge_id = ? AND badge_tier = ?;
        """;

        try (
            PreparedStatement statement1 = connection.prepareStatement(removeAllUpdate);
            PreparedStatement statement2 = connection.prepareStatement(restoreBadgeUpdate)
        ) {
            statement1.executeUpdate();

            for (String badgeID : BADGES.keySet()) {
                BadgeItemConfig badge = BADGES.get(badgeID);

                for (int i=0; i<badge.getFiles().length; i++) {
                    statement2.setString(1, badgeID);
                    statement2.setInt(2, i);
                    statement2.addBatch();
                }
            }

            statement2.executeBatch();
        }
    }

    private static void fixInfoTables(String databaseType, Connection connection) throws SQLException {
        String truncateTable = switch (databaseType) {
            case "rarities" -> "TRUNCATE rarities_info;";
            case "boars" -> "TRUNCATE boars_info;";
            default -> null;
        };

        if (truncateTable == null) {
            return;
        }

        String curUpdate = switch (databaseType) {
            case "rarities" -> """
                INSERT INTO rarities_info (rarity_id, prior_rarity_id, base_bucks, researcher_need)
                VALUES (?, ?, ?, ?);
            """;
            case "boars" -> """
                INSERT INTO boars_info (boar_id, rarity_id, is_skyblock)
                VALUES (?, ?, ?);
            """;
            default -> null;
        };

        try (
            PreparedStatement statement1 = connection.prepareStatement(truncateTable);
            PreparedStatement statement2 = connection.prepareStatement(curUpdate)
        ) {
            statement1.setString(1, databaseType + "_info");
            statement1.executeUpdate();

            if (databaseType.equals("rarities")) {
                addRarityBatches(statement2);
            } else {
                addBoarBatches(statement2);
            }

            statement2.executeBatch();
        }
    }

    private static void addBoarBatches(PreparedStatement statement) throws SQLException {
        for (String boarID : BOARS.keySet()) {
            BoarItemConfig boar = BOARS.get(boarID);

            if (boar.isBlacklisted()) {
                continue;
            }

            String rarityID = BoarUtil.findRarityKey(boarID);

            statement.setString(1, boarID);
            statement.setString(2, rarityID);
            statement.setBoolean(3, boar.isSB());
            statement.addBatch();
        }
    }

    private static void addRarityBatches(PreparedStatement statement) throws SQLException {
        String priorRarityID = null;

        for (String rarityID : RARITIES.keySet()) {
            RarityConfig rarityConfig = RARITIES.get(rarityID);

            if (priorRarityID != null) {
                priorRarityID = "'%s'".formatted(priorRarityID);
            }

            statement.setString(1, rarityID);
            statement.setString(2, priorRarityID);
            statement.setInt(3, rarityConfig.getBaseBucks());
            statement.setBoolean(4, rarityConfig.isResearcherNeed());
            statement.addBatch();

            priorRarityID = rarityID;
        }
    }
}
