package dev.boarbot.util.boar;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.util.time.TimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public final class BoarUtil {
    public static String findRarityKey(String boarID) {
        BotConfig config = BoarBotApp.getBot().getConfig();

        for (String rarityKey : config.getRarityConfigs().keySet()) {
            boolean boarNotExist = !Arrays.asList(config.getRarityConfigs().get(rarityKey).getBoars()).contains(boarID);

            if (boarNotExist) {
                continue;
            }

            return rarityKey;
        }

        throw new IllegalArgumentException("Boar ID input does not exist");
    }

    public static List<String> getRandBoarIDs(
        long multiplier, String guildID, Connection connection
    ) throws SQLException {
        BotConfig config = BoarBotApp.getBot().getConfig();

        List<String> boarsObtained = new ArrayList<>();

        Map<String, Double> weights = new HashMap<>();
        double maxWeight = 0;
        double totalWeight = 0;

        double newMaxWeight = 0;
        String newMaxWeightKey = "";

        Map<String, RarityConfig> rarities = config.getRarityConfigs();
        int rarityIncreaseConst = config.getNumberConfig().getRarityIncreaseConst();

        for (String rarityKey : rarities.keySet()) {
            double weight = rarities.get(rarityKey).getWeight();

            maxWeight = Math.max(maxWeight, weight);
            weights.put(rarityKey, rarities.get(rarityKey).getWeight());
        }

        for (String weightKey : weights.keySet()) {
            double weight = weights.get(weightKey);

            if ((TimeUtil.isHalloween() || TimeUtil.isChristmas()) && weightKey.equals("common")) {
                weight = 0;
                weights.put(weightKey, weight);
            }

            if (!TimeUtil.isHalloween() && weightKey.equals("halloween")) {
                weight = 0;
                weights.put(weightKey, weight);
            }

            if (!TimeUtil.isChristmas() && weightKey.equals("christmas")) {
                weight = 0;
                weights.put(weightKey, weight);
            }

            if (weight == 0) {
                continue;
            }

            weight = weight * (
                Math.atan(((multiplier - 1) * weight) / rarityIncreaseConst) * (maxWeight - weight) / weight + 1
            );

            if (weight > newMaxWeight) {
                newMaxWeight = weight;
                newMaxWeightKey = weightKey;
            }

            totalWeight += weight;
            weights.put(weightKey, weight);
        }

        double truthWeight = 0;

        if (multiplier >= 100000) {
            truthWeight = totalWeight / 50;
        } else if (multiplier >= 10000) {
            truthWeight = totalWeight / 750;
        } else if (multiplier >= 1000) {
            truthWeight = totalWeight / 10000;
        }

        weights.put("truth", truthWeight);
        weights.put(newMaxWeightKey, newMaxWeight - truthWeight);

        Map<String, Double> sortedWeights = weights.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new
            ));

        String lastWeightKey = sortedWeights.keySet().stream().toList().getLast();
        int numBoars = 1;

        for (int i=3; i<6; i++) {
            if (Math.random() < multiplier / Math.pow(10, i)) {
                numBoars++;
            }
        }

        for (int i=0; i<numBoars; i++) {
            double randRarity = Math.random();
            double priorWeightTotal = 0;

            for (Map.Entry<String, Double> entry : sortedWeights.entrySet()) {
                double curRandNeeded = (entry.getValue() + priorWeightTotal) / totalWeight;

                if (randRarity > curRandNeeded && !entry.getKey().equals(lastWeightKey)) {
                    priorWeightTotal += entry.getValue();
                    continue;
                }

                String boarObtained = BoarUtil.findValid(entry.getKey(), guildID, connection);
                boarsObtained.add(boarObtained);
                break;
            }
        }

        return boarsObtained;
    }

    public static String findValid(String rarityKey, String guildID, Connection connection) throws SQLException {
        BotConfig config = BoarBotApp.getBot().getConfig();
        RarityConfig rarityConfig = config.getRarityConfigs().get(rarityKey);

        double randBoar = Math.random();
        List<String> validBoars = new ArrayList<>();

        String query = """
            SELECT is_skyblock_community
            FROM guilds
            WHERE guild_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, guildID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    boolean isSkyblockGuild = results.getBoolean("is_skyblock_community");

                    for (String boarID : rarityConfig.getBoars()) {
                        IndivItemConfig boarConfig = config.getItemConfig().getBoars().get(boarID);
                        boolean blacklisted = boarConfig.getBlacklisted() != null && boarConfig.getBlacklisted();
                        boolean isSkyblockBoar = boarConfig.getIsSB() != null && boarConfig.getIsSB();

                        if (blacklisted || isSkyblockBoar && !isSkyblockGuild) {
                            continue;
                        }

                        validBoars.add(boarID);
                    }
                }
            }
        }

        if (validBoars.isEmpty()) {
            throw new IllegalArgumentException("Unable to find a boar ID for rarity: " + rarityKey);
        }

        return validBoars.get((int) (randBoar * validBoars.size()));
    }
}
