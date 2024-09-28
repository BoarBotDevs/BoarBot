package dev.boarbot.bot;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.items.BadgeItemConfig;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.QuestDataUtil;
import dev.boarbot.util.logging.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

class DatabaseLoader implements Configured {
    public static void loadIntoDatabase() {
        fixBoarInfo();
        fixRarityInfo();
        fixBadges();
        fixQuests();
    }

    private static void fixBoarInfo() {
        Log.debug(DatabaseLoader.class, "Loading boars into the database...");

        try (Connection connection = DataUtil.getConnection()) {
            String query = """
                SELECT boar_id, rarity_id, is_skyblock
                FROM boars_info;
            """;

            String fixQuery = """
                UPDATE boars_info
                SET rarity_id = ?, is_skyblock = ?
                WHERE boar_id = ?;
            """;

            String removeQuery = """
                DELETE FROM boars_info
                WHERE boar_id = ?;
            """;

            String addQuery = """
                INSERT INTO boars_info (boar_id, rarity_id, is_skyblock)
                VALUES (?, ?, ?);
            """;

            Set<String> invalidBoarIDs = new HashSet<>();
            Set<String> fixBoarIDs = new HashSet<>();
            Set<String> addBoarIDs = new HashSet<>(BOARS.keySet());

            try (
                PreparedStatement statement1 = connection.prepareStatement(query);
                PreparedStatement statement2 = connection.prepareStatement(fixQuery);
                PreparedStatement statement3 = connection.prepareStatement(removeQuery);
                PreparedStatement statement4 = connection.prepareStatement(addQuery)
            ) {
                try (ResultSet resultSet = statement1.executeQuery()) {
                    while (resultSet.next()) {
                        String boarID = resultSet.getString("boar_id");
                        String rarityID = resultSet.getString("rarity_id");
                        String foundRarityID = BoarUtil.findRarityKey(boarID);
                        boolean isSkyblock = resultSet.getBoolean("is_skyblock");

                        boolean boarExists = BOARS.containsKey(boarID) && !BOARS.get(boarID).isBlacklisted();
                        boolean needsFixed = boarExists &&
                            (!rarityID.equals(foundRarityID) || isSkyblock != BOARS.get(boarID).isSB());

                        if (!boarExists) {
                            invalidBoarIDs.add(boarID);
                        } else if (needsFixed) {
                            fixBoarIDs.add(boarID);
                        }

                        addBoarIDs.remove(boarID);
                    }
                }

                for (String boarID : fixBoarIDs) {
                    statement2.setString(1, BoarUtil.findRarityKey(boarID));
                    statement2.setBoolean(2, BOARS.get(boarID).isSB());
                    statement2.setString(3, boarID);
                    statement2.addBatch();
                }

                for (String boarID : invalidBoarIDs) {
                    statement3.setString(1, boarID);
                    statement3.addBatch();
                }

                for (String boarID : addBoarIDs) {
                    statement4.setString(1, boarID);
                    statement4.setString(2, BoarUtil.findRarityKey(boarID));
                    statement4.setBoolean(3, BOARS.get(boarID).isSB());
                    statement4.addBatch();
                }

                statement2.executeBatch();
                statement3.executeBatch();
                statement4.executeBatch();
            }
        } catch (SQLException exception) {
            Log.error(DatabaseLoader.class, "Something went wrong when loading boars into database", exception);
            System.exit(-1);
        }

        Log.debug(DatabaseLoader.class, "Loaded boars into the database...");
    }

    private static void fixRarityInfo() {
        Log.debug(DatabaseLoader.class, "Loading rarities into the database...");

        try (Connection connection = DataUtil.getConnection()) {
            String query = """
                SELECT rarity_id, prior_rarity_id, base_bucks, researcher_need
                FROM rarities_info;
            """;

            String fixQuery = """
                UPDATE rarities_info
                SET prior_rarity_id = ?, base_bucks = ?, researcher_need = ?
                WHERE rarity_id = ?;
            """;

            String removeQuery = """
                DELETE FROM rarities_info
                WHERE rarity_id = ?;
            """;

            String addQuery = """
                INSERT INTO rarities_info (rarity_id, prior_rarity_id, base_bucks, researcher_need)
                VALUES (?, ?, ?, ?);
            """;

            Set<String> invalidRarityIDs = new HashSet<>();
            Set<String> fixRarityIDs = new HashSet<>();
            Set<String> addRarityIDs = new HashSet<>(RARITIES.keySet());

            try (
                PreparedStatement statement1 = connection.prepareStatement(query);
                PreparedStatement statement2 = connection.prepareStatement(fixQuery);
                PreparedStatement statement3 = connection.prepareStatement(removeQuery);
                PreparedStatement statement4 = connection.prepareStatement(addQuery)
            ) {
                try (ResultSet resultSet = statement1.executeQuery()) {
                    while (resultSet.next()) {
                        String rarityID = resultSet.getString("rarity_id");
                        String priorRarityID = resultSet.getString("prior_rarity_id");
                        int baseBucks = resultSet.getInt("base_bucks");
                        boolean researcherNeed = resultSet.getBoolean("researcher_need");

                        boolean rarityExists = RARITIES.containsKey(rarityID);
                        boolean needsFixed = rarityExists && (
                            baseBucks != RARITIES.get(rarityID).getBaseBucks() ||
                            researcherNeed != RARITIES.get(rarityID).isResearcherNeed()
                        );

                        if (priorRarityID == null) {
                            needsFixed = needsFixed || BoarUtil.getPriorRarityKey(rarityID) != null;
                        } else {
                            needsFixed = needsFixed || !priorRarityID.equals(BoarUtil.getPriorRarityKey(rarityID));
                        }

                        if (!rarityExists) {
                            invalidRarityIDs.add(rarityID);
                        } else if (needsFixed) {
                            fixRarityIDs.add(rarityID);
                        }

                        addRarityIDs.remove(rarityID);
                    }
                }

                for (String rarityID : fixRarityIDs) {
                    statement2.setString(1, BoarUtil.getPriorRarityKey(rarityID));
                    statement2.setInt(2, RARITIES.get(rarityID).getBaseBucks());
                    statement2.setBoolean(3, RARITIES.get(rarityID).isResearcherNeed());
                    statement2.setString(4, rarityID);
                    statement2.addBatch();
                }

                for (String rarityID : invalidRarityIDs) {
                    statement3.setString(1, rarityID);
                    statement3.addBatch();
                }

                for (String rarityID : addRarityIDs) {
                    statement4.setString(1, rarityID);
                    statement4.setString(2, BoarUtil.getPriorRarityKey(rarityID));
                    statement4.setInt(3, RARITIES.get(rarityID).getBaseBucks());
                    statement4.setBoolean(4, RARITIES.get(rarityID).isResearcherNeed());
                    statement4.addBatch();
                }

                statement2.executeBatch();
                statement3.executeBatch();
                statement4.executeBatch();
            }
        } catch (SQLException exception) {
            Log.error(DatabaseLoader.class, "Something went wrong when loading rarities into database", exception);
            System.exit(-1);
        }

        Log.debug(DatabaseLoader.class, "Loaded rarities into the database");
    }

    private static void fixQuests() {
        try (Connection connection = DataUtil.getConnection()) {
            if (QuestDataUtil.needNewQuests(connection)) {
                Log.debug(DatabaseLoader.class, "Loading quests into the database...");
                QuestDataUtil.updateQuests(connection);
                Log.debug(DatabaseLoader.class, "Loaded quests into the database");
            }
        } catch (SQLException exception) {
            Log.error(DatabaseLoader.class, "Something went wrong when loading quests into database", exception);
            System.exit(-1);
        }
    }

    private static void fixBadges() {
        Log.debug(DatabaseLoader.class, "Loading badges into the database...");

        try (Connection connection = DataUtil.getConnection()) {
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

                    for (int i=-1; i<badge.getFiles().length; i++) {
                        statement2.setString(1, badgeID);
                        statement2.setInt(2, i);
                        statement2.addBatch();
                    }
                }

                statement2.executeBatch();
            }
        } catch (SQLException exception) {
            Log.error(DatabaseLoader.class, "Something went wrong when loading badges into database", exception);
            System.exit(-1);
        }

        Log.debug(DatabaseLoader.class, "Loaded badges into the database");
    }
}
