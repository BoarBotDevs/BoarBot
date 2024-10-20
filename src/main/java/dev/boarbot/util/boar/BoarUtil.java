package dev.boarbot.util.boar;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.bot.config.prompts.PromptConfig;
import dev.boarbot.util.time.TimeUtil;

import java.util.*;
import java.util.stream.Collectors;

public final class BoarUtil implements Configured {
    public static String findRarityKey(String boarID) {
        for (String rarityKey : RARITIES.keySet()) {
            boolean boarNotExist = !Arrays.asList(RARITIES.get(rarityKey).getBoars()).contains(boarID);

            if (boarNotExist) {
                continue;
            }

            return rarityKey;
        }

        throw new IllegalArgumentException("Boar ID (%s) input does not exist".formatted(boarID));
    }

    public static String getPriorRarityKey(String rarityKey) {
        Iterator<String> iterator = RARITIES.keySet().iterator();
        String priorRarityKey = null;

        while (iterator.hasNext()) {
            String curRarityKey = iterator.next();

            if (curRarityKey.equals(rarityKey)) {
                break;
            }

            priorRarityKey = curRarityKey;
        }

        return priorRarityKey;
    }

    public static String getNextRarityKey(String rarityKey) {
        Iterator<String> iterator = RARITIES.keySet().iterator();

        while (!iterator.next().equals(rarityKey));

        return iterator.next();
    }

    public static String getHigherRarity(String rarity1, String rarity2) {
        for (String rarityID : RARITIES.keySet()) {
            if (rarity1 == null || rarityID.equals(rarity1)) {
                return rarity2;
            }

            if (rarity2 == null || rarityID.equals(rarity2)) {
                return rarity1;
            }
        }

        return rarity1;
    }

    public static List<String> getRandBoarIDs(long blessings, boolean isSkyblockGuild) {
        List<String> boarsObtained = new ArrayList<>();

        Map<String, Double> weights = new HashMap<>();
        double maxWeight = 0;
        double totalWeight = 0;

        double newMaxWeight = 0;
        String newMaxWeightKey = "";

        int rarityIncreaseConst = NUMS.getRarityIncreaseConst();

        for (String rarityKey : RARITIES.keySet()) {
            double weight = RARITIES.get(rarityKey).getWeight();

            maxWeight = Math.max(maxWeight, weight);
            weights.put(rarityKey, RARITIES.get(rarityKey).getWeight());
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
                Math.atan((blessings * weight) / rarityIncreaseConst) * (maxWeight - weight) / weight + 1
            );

            if (weight > newMaxWeight) {
                newMaxWeight = weight;
                newMaxWeightKey = weightKey;
            }

            totalWeight += weight;
            weights.put(weightKey, weight);
        }

        double truthWeight = 0;

        if (blessings >= 100000) {
            truthWeight = totalWeight / 50;
        } else if (blessings >= 10000) {
            truthWeight = totalWeight / 750;
        } else if (blessings >= 1000) {
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
                )
            );

        String lastWeightKey = sortedWeights.keySet().stream().toList().getLast();
        int numBoars = 1;

        for (int i=3; i<6; i++) {
            if (Math.random() < blessings / Math.pow(10, i)) {
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

                String boarObtained = BoarUtil.findValid(entry.getKey(), isSkyblockGuild);
                boarsObtained.add(boarObtained);
                break;
            }
        }

        return boarsObtained;
    }

    public static String findValid(String rarityKey, boolean isSkyblockGuild) {
        RarityConfig rarityConfig = RARITIES.get(rarityKey);

        double randBoar = Math.random();
        List<String> validBoars = new ArrayList<>();

        for (String boarID : rarityConfig.getBoars()) {
            BoarItemConfig boarConfig = BOARS.get(boarID);
            boolean blacklisted = boarConfig.isBlacklisted();
            boolean isSecret = boarConfig.isSecret();
            boolean isSkyblockBoar = boarConfig.isSB();

            if (blacklisted || isSecret || isSkyblockBoar && !isSkyblockGuild) {
                continue;
            }

            validBoars.add(boarID);
        }

        if (validBoars.isEmpty()) {
            throw new IllegalArgumentException("Unable to find a boar ID for rarity: " + rarityKey);
        }

        return validBoars.get((int) (randBoar * validBoars.size()));
    }

    public static String getPromptStr(String promptID) {
        for (PromptConfig promptType : CONFIG.getPromptConfig().values()) {
            for (String prompt : promptType.getPrompts().keySet()) {
                if (promptID.equals(prompt)) {
                    return "%s - %s".formatted(promptType.getName(), promptType.getPrompts().get(prompt).getName());
                }
            }
        }

        return STRS.getUnavailable();
    }

    public static int getTotalUniques() {
        int num = 0;

        for (BoarItemConfig boar : BOARS.values()) {
            if (!boar.isBlacklisted()) {
                num++;
            }
        }

        return num;
    }

    public static int getNumRarityBoars(RarityConfig rarityConfig) {
        int num = 0;

        for (String boarID : rarityConfig.getBoars()) {
            if (!BOARS.get(boarID).isBlacklisted()) {
                num++;
            }
        }

        return num;
    }
}
