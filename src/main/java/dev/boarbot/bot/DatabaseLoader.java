package dev.boarbot.bot;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
class DatabaseLoader {
    public static void loadIntoDatabase(String databaseType) {
        BotConfig config = BoarBotApp.getBot().getConfig();
        
        try (
            Connection connection = DataUtil.getConnection();
            Statement statement = connection.createStatement()
        ) {
            StringBuilder sqlStatement = new StringBuilder();

            if (databaseType.equals("rarities")) {
                String resetQuery = """
                    DELETE FROM rarities_info
                    WHERE rarity_id = 'all_done';
                """;

                statement.executeUpdate(resetQuery);
            }

            String tableColumns = "(boar_id, rarity_id, is_skyblock)";

            if (databaseType.equals("rarities")) {
                tableColumns = "(rarity_id, prior_rarity_id, base_bucks, hunter_need)";
            } else if (databaseType.equals("quests")) {
                tableColumns = "(quest_id, easy_value, medium_value, hard_value, very_hard_value, value_type)";
            }

            sqlStatement.append("DELETE FROM %s_info;".formatted(databaseType));

            statement.executeUpdate(sqlStatement.toString());
            sqlStatement.setLength(0);

            sqlStatement.append("INSERT INTO %s_info %s VALUES ".formatted(databaseType, tableColumns));

            switch (databaseType) {
                case "boars" -> {
                    for (String boarID : config.getItemConfig().getBoars().keySet()) {
                        if (config.getItemConfig().getBoars().get(boarID).isBlacklisted()) {
                            continue;
                        }

                        int isSB = config.getItemConfig().getBoars().get(boarID).isSB() ? 1 : 0;
                        String rarityID = BoarUtil.findRarityKey(boarID);

                        sqlStatement.append("('%s','%s',%d),".formatted(boarID, rarityID, isSB));
                    }
                }
                case "rarities" -> {
                    String priorRarityID = null;
                    for (String rarityID : config.getRarityConfigs().keySet()) {
                        int score = config.getRarityConfigs().get(rarityID).getBaseScore();
                        int hunterNeed = config.getRarityConfigs().get(rarityID).isHunterNeed() ? 1 : 0;

                        sqlStatement.append("('%s','%s',%d,%d),".formatted(rarityID, priorRarityID, score, hunterNeed));
                        priorRarityID = rarityID;
                    }
                }
            }

            sqlStatement.setLength(sqlStatement.length() - 1);
            sqlStatement.append(";");

            statement.executeUpdate(sqlStatement.toString());

            if (databaseType.equals("boars")) {
                String resetQuery = """
                    INSERT INTO rarities_info (rarity_id, prior_rarity_id, base_bucks, hunter_need)
                    VALUES ('all_done', null, 0, 0)
                """;

                statement.executeUpdate(resetQuery);
            }
        } catch (SQLException exception) {
            log.error("Something went wrong when loading config data into database.", exception);
            System.exit(-1);
        }
    }
}
