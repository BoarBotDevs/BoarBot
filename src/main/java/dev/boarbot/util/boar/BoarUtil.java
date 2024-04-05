package dev.boarbot.util.boar;

import dev.boarbot.bot.config.BotConfig;

import java.util.Arrays;

public final class BoarUtil {
    public static int findRarityIndex(String boarID, BotConfig config) {
        for (int i=0; i<config.getRarityConfigs().length; i++) {
            boolean boarNotExist = !Arrays.asList(config.getRarityConfigs()[i].getBoars()).contains(boarID);

            if (boarNotExist) {
                continue;
            }

            return i;
        }

        throw new IllegalArgumentException("Boar ID input does not exist");
    }
}
