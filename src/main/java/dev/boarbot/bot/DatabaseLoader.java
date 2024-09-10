package dev.boarbot.bot;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BadgeItemConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
class DatabaseLoader {
    private final static BotConfig config = BoarBotApp.getBot().getConfig();

    public static void loadIntoDatabase(String databaseType) {
        try (
            Connection connection = DataUtil.getConnection();
            Statement statement = connection.createStatement()
        ) {
            if (databaseType.equals("rarities")) {
                String resetQuery = """
                    DELETE FROM rarities_info
                    WHERE rarity_id = 'all_done';
                """;

                statement.executeUpdate(resetQuery);
            }

            if (databaseType.equals("badges")) {
                fixBadges(statement);
            } else {
                fixInfoTables(databaseType, statement);
            }

            if (databaseType.equals("boars")) {
                String resetQuery = """
                    INSERT INTO rarities_info (rarity_id, prior_rarity_id, base_bucks, researcher_need)
                    VALUES ('all_done', null, 0, 0)
                """;

                statement.executeUpdate(resetQuery);
            }
        } catch (SQLException exception) {
            log.error("Something went wrong when loading config data into database.", exception);
            System.exit(-1);
        }
    }

    private static void fixBadges(Statement statement) throws SQLException {
        String removeAllUpdate = """
            UPDATE collected_badges
            SET `exists` = false;
        """;

        statement.executeUpdate(removeAllUpdate);

        for (String badgeID : config.getItemConfig().getBadges().keySet()) {
            BadgeItemConfig badge = config.getItemConfig().getBadges().get(badgeID);

            for (int i=0; i<badge.getFiles().length; i++) {
                String restoreBadgeUpdate = """
                    UPDATE collected_badges
                    SET `exists` = true
                    WHERE badge_id = '%s' AND badge_tier = '%d';
                """.formatted(badgeID, i);

                statement.executeUpdate(restoreBadgeUpdate);
            }
        }
    }

    private static void fixInfoTables(String databaseType, Statement statement) throws SQLException {
        StringBuilder sqlStatement = new StringBuilder();

        sqlStatement.append("TRUNCATE %s_info;".formatted(databaseType));
        statement.executeUpdate(sqlStatement.toString());
        sqlStatement.setLength(0);

        String tableColumns = getTableColumns(databaseType);

        sqlStatement.append("INSERT INTO %s_info %s VALUES ".formatted(databaseType, tableColumns));
        appendQuery(databaseType, sqlStatement);
        sqlStatement.setLength(sqlStatement.length() - 1);
        sqlStatement.append(";");

        statement.executeUpdate(sqlStatement.toString());
    }

    private static String getTableColumns(String databaseType) {
        if (databaseType.equals("boars")) {
            return "(boar_id, rarity_id, is_skyblock)";
        }

        return "(rarity_id, prior_rarity_id, base_bucks, researcher_need)";
    }

    private static void appendQuery(String databaseType, StringBuilder sqlStatement) {
        if (databaseType.equals("boars")) {
            appendBoarQuery(sqlStatement);
        } else if (databaseType.equals("rarities")) {
            appendRarityQuery(sqlStatement);
        }
    }

    private static void appendBoarQuery(StringBuilder sqlStatement) {
        for (String boarID : config.getItemConfig().getBoars().keySet()) {
            BoarItemConfig boar = config.getItemConfig().getBoars().get(boarID);

            if (boar.isBlacklisted()) {
                continue;
            }

            int isSB = boar.isSB() ? 1 : 0;
            String rarityID = BoarUtil.findRarityKey(boarID);

            sqlStatement.append("('%s','%s',%d),".formatted(boarID, rarityID, isSB));
        }
    }

    private static void appendRarityQuery(StringBuilder sqlStatement) {
        String priorRarityID = null;
        for (String rarityID : config.getRarityConfigs().keySet()) {
            RarityConfig rarityConfig = config.getRarityConfigs().get(rarityID);
            int score = rarityConfig.getBaseScore();
            int researcherNeed = rarityConfig.isResearcherNeed() ? 1 : 0;

            if (priorRarityID != null) {
                priorRarityID = "'%s'".formatted(priorRarityID);
            }

            sqlStatement.append("('%s',%s,%d,%d),".formatted(rarityID, priorRarityID, score, researcherNeed));
            priorRarityID = rarityID;
        }
    }
}
