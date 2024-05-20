package dev.boarbot.util.boar;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;

import java.util.Arrays;

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
}
