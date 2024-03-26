package dev.boarbot.entities.boaruser;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.prompts.PromptTypeConfig;
import dev.boarbot.entities.boaruser.collectibles.CollectedPowerup;
import dev.boarbot.entities.boaruser.stats.GeneralStats;
import dev.boarbot.entities.boaruser.stats.PromptStats;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.User;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Log4j2
public class BoarUser {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    @Getter private final User user;
    @Getter private BoarUserData data = new BoarUserData();

    public BoarUser(User user) throws IOException {
        this(user, false);
    }

    public BoarUser(User user, boolean createFile) throws IOException {
        this.user = user;

        refreshUserData(createFile);

        boolean shouldFixData = createFile || this.data.getStats().getGeneral().getFirstDaily() > 0 ||
            this.data.getStats().getGeneral().getTotalBoars() > 0 ||
            !this.data.getItemCollection().getBadges().isEmpty();

        if (shouldFixData) {
            fixUserData();
        }
    }

    public void refreshUserData(boolean createFile) throws IOException {
        this.data = getUserData(createFile);
    }

    private BoarUserData getUserData(boolean createFile) throws IOException {
        StringBuilder userDataJSON = new StringBuilder();
        String userFile = config.getPathConfig().getDatabaseFolder() +
            config.getPathConfig().getUserDataFolder() + this.user.getId() + ".json";

        File file = new File(userFile);
        Gson g = new Gson();

        try {
            Scanner reader = new Scanner(file);
            while(reader.hasNextLine()) {
                userDataJSON.append(reader.nextLine());
            }
        } catch (FileNotFoundException exception) {
            userDataJSON.append(g.toJson(this.data));

            if (createFile) {
                log.info("New user! - [%s] (%s)".formatted(this.user.getName(), this.user.getId()));

                BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
                writer.write(userDataJSON.toString());
                writer.close();
            }
        }

        return g.fromJson(userDataJSON.toString(), BoarUserData.class);
    }

    private void fixUserData() throws IOException {
        String userFile = config.getPathConfig().getDatabaseFolder() +
            config.getPathConfig().getUserDataFolder() + this.user.getId() + ".json";

        Set<String> boarGottenIDs = this.data.getItemCollection().getBoars().keySet();
        long beginningOfDay = LocalDate.now()
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli();
        long twoDailiesAgo = beginningOfDay - config.getNumberConfig().getOneDay() * 2L;

        NumberConfig nums = config.getNumberConfig();
        GeneralStats genStats = this.data.getStats().getGeneral();

        // Removes invalid boar IDs from collection
        for (String boarID : boarGottenIDs) {
            if (config.getItemConfig().getBoars().containsKey(boarID)) {
                continue;
            }

            genStats.setTotalBoars(
                genStats.getTotalBoars() - this.data.getItemCollection().getBoars().get(boarID).getNum()
            );

            this.getData().getItemCollection().getBoars().remove(boarID);

            if (genStats.getLastBoar().equals(boarID)) {
                genStats.setLastBoar("");
            }

            if (genStats.getFavoriteBoar().equals(boarID)) {
                genStats.setFavoriteBoar("");
            }
        }

        Map<String, Map<String, PromptStats>> promptData = this.data.getStats().getPowerups().getPrompts();
        Map<String, PromptTypeConfig> promptTypes = this.config.getPromptConfig().getTypes();

        // Adds prompt data to user file if it doesn't exist
        for (String promptType : promptTypes.keySet()) {
            promptData.computeIfAbsent(promptType, key -> new HashMap<>());
        }

        // Removes prompt or prompt type from data if not in config
        for (String promptType : promptData.keySet()) {
            if (promptTypes.get(promptType) == null) {
                promptData.remove(promptType);
                continue;
            }

            for (String promptID : promptData.get(promptType).keySet()) {
                if (promptTypes.get(promptType).getPrompts().get(promptID) == null) {
                    promptData.get(promptType).remove(promptID);
                }
            }
        }

        // TODO: Add quest data to all user files

        // TODO: Check quests from global quests file

        Map<String, CollectedPowerup> powerupData = this.data.getItemCollection().getPowerups();

        if (powerupData.get("miracle") == null) {
            powerupData.put("miracle", new CollectedPowerup());
            powerupData.get("miracle").setNumActive(0);
        }

        if (powerupData.get("gift") == null) {
            powerupData.put("gift", new CollectedPowerup());
            powerupData.get("gift").setNumOpened(0);
        }

        if (powerupData.get("enhancer") == null) {
            powerupData.put("enhancer", new CollectedPowerup());
            powerupData.get("enhancer").setRaritiesUsed(new Integer[]{0,0,0,0,0,0,0});
        }

        if (powerupData.get("clone") == null) {
            powerupData.put("clone", new CollectedPowerup());
            powerupData.get("clone").setNumSuccess(0);
            powerupData.get("clone").setRaritiesUsed(new Integer[]{0,0,0,0,0,0,0,0,0,0});
        }

        // TODO: Fix raritiesUsed in all user files

        powerupData.get("miracle").setNumTotal(
            Math.max(0, Math.min(powerupData.get("miracle").getNumTotal(), nums.getMaxPowBase()))
        );
        powerupData.get("miracle").setHighestTotal(
            Math.max(0, Math.min(powerupData.get("miracle").getHighestTotal(), nums.getMaxPowBase()))
        );

        powerupData.get("gift").setNumTotal(
            Math.max(0, Math.min(powerupData.get("gift").getNumTotal(), nums.getMaxSmallPow()))
        );
        powerupData.get("gift").setHighestTotal(
            Math.max(0, Math.min(powerupData.get("gift").getHighestTotal(), nums.getMaxSmallPow()))
        );

        powerupData.get("enhancer").setNumTotal(
            Math.max(0, Math.min(powerupData.get("enhancer").getNumTotal(), nums.getMaxEnhancers()))
        );
        powerupData.get("enhancer").setHighestTotal(
            Math.max(0, Math.min(powerupData.get("enhancer").getHighestTotal(), nums.getMaxEnhancers()))
        );

        powerupData.get("clone").setNumTotal(
            Math.max(0, Math.min(powerupData.get("clone").getNumTotal(), nums.getMaxSmallPow()))
        );
        powerupData.get("clone").setHighestTotal(
            Math.max(0, Math.min(powerupData.get("clone").getHighestTotal(), nums.getMaxSmallPow()))
        );

        // TODO: Add highestStreak to all user files

        if (genStats.getLastDaily() < twoDailiesAgo) {
            genStats.setBoarStreak(0);
        }

        genStats.setHighestStreak(Math.max(genStats.getBoarStreak(), genStats.getHighestStreak()));

        // TODO: Remove unbanTime from all user files

        int uniques = 0;

        for (String boarID : this.data.getItemCollection().getBoars().keySet()) {
            boolean hasBoar = this.data.getItemCollection().getBoars().get(boarID).getNum() > 0;
            RarityConfig[] rarityConfigs = this.config.getRarityConfigs();
            boolean isSpecial = Arrays.asList(rarityConfigs[rarityConfigs.length - 1].getBoars()).contains(boarID);

            if (hasBoar && !isSpecial) {
                uniques++;
            }
        }

        genStats.setMultiplier(uniques + genStats.getHighestStreak());
        genStats.setMultiplier(Math.min(genStats.getMultiplier(), nums.getMaxPowBase()));

        long visualMulti = genStats.getMultiplier() + 1;
        int numMiraclesActive = powerupData.get("miracle").getNumActive();

        for (int i=0; i<numMiraclesActive; i++) {
            visualMulti += Math.min((long) Math.ceil(visualMulti * 0.1), nums.getMiracleIncreaseMax());
        }

        visualMulti--;

        genStats.setHighestMulti(Math.max(visualMulti, genStats.getHighestMulti()));
        genStats.setBoarScore(Math.max(0, Math.min(genStats.getBoarScore(), nums.getMaxScore())));

        Gson g = new Gson();
        BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
        writer.write(g.toJson(this.data));
        writer.close();
    }

    public void updateUserData() throws IOException {
        BoarUserData userData = this.getUserData(false);

        // TODO: Quest stuff

        Map<String, CollectedPowerup> powerupData = this.data.getItemCollection().getPowerups();
        Map<String, CollectedPowerup> filePowerupData = userData.getItemCollection().getPowerups();

        for (String powerupID : powerupData.keySet()) {
            powerupData.get(powerupID).setHighestTotal(
                Math.max(powerupData.get(powerupID).getNumTotal(), powerupData.get(powerupID).getHighestTotal())
            );
            powerupData.get(powerupID).setNumClaimed(
                Math.max(0, powerupData.get(powerupID).getNumTotal() - filePowerupData.get(powerupID).getNumTotal())
            );
        }

        this.fixUserData();
    }
}
