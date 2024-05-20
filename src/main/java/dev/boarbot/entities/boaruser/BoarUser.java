package dev.boarbot.entities.boaruser;

import com.google.gson.Gson;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.RarityConfig;
import dev.boarbot.bot.config.prompts.PromptTypeConfig;
import dev.boarbot.entities.boaruser.data.collectibles.CollectedBoar;
import dev.boarbot.entities.boaruser.data.collectibles.CollectedPowerup;
import dev.boarbot.entities.boaruser.data.BoarUserData;
import dev.boarbot.entities.boaruser.data.stats.GeneralStats;
import dev.boarbot.entities.boaruser.data.stats.PromptStats;
import dev.boarbot.entities.boaruser.data.stats.QuestStats;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.ItemsDataUtil;
import dev.boarbot.util.data.QuestsDataUtil;
import dev.boarbot.util.data.types.QuestData;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.User;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Log4j2
public class BoarUser {
    private final BotConfig config = BoarBotApp.getBot().getConfig();

    @Getter private final User user;
    @Getter private final String userID;
    @Getter private BoarUserData data = new BoarUserData();

    private volatile int numRefs = 1;

    public BoarUser(User user) throws IOException {
        this(user, user.getId(), false);
    }

    public BoarUser(String userID) throws IOException {
        this(null, userID, false);
    }

    public BoarUser(User user, boolean createFile) throws IOException {
        this(user, user.getId(), createFile);
    }

    public BoarUser(User user, String userID, boolean createFile) throws IOException {
        this.user = user;
        this.userID = userID;

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
            config.getPathConfig().getUserDataFolder() + userID + ".json";

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
                log.info(
                    "New user! - [%s] (%s)".formatted(this.user == null ? "N/A" : this.user.getName(), this.userID)
                );

                BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
                writer.write(userDataJSON.toString());
                writer.close();
            }
        }

        return g.fromJson(userDataJSON.toString(), BoarUserData.class);
    }

    private void fixUserData() throws IOException {
        String userFile = config.getPathConfig().getDatabaseFolder() +
            config.getPathConfig().getUserDataFolder() + this.userID + ".json";

        Set<String> boarGottenIDs = new HashSet<>(this.data.getItemCollection().getBoars().keySet());
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

            this.data.getItemCollection().getBoars().remove(boarID);

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
            promptData.putIfAbsent(promptType, new HashMap<>());
        }

        // Removes prompt or prompt type from data if not in config
        for (String promptType : new HashSet<>(promptData.keySet())) {
            if (promptTypes.get(promptType) == null) {
                promptData.remove(promptType);
                continue;
            }

            for (String promptID : new HashSet<>(promptData.get(promptType).keySet())) {
                if (promptTypes.get(promptType).getPrompts().get(promptID) == null) {
                    promptData.get(promptType).remove(promptID);
                }
            }
        }

        // TODO: Add quest data to all user files

        QuestData questData = new QuestData();
        QuestStats questStats = this.data.getStats().getQuests();

        if (questStats.getQuestWeekStart() != questData.getQuestsStartTimestamp()) {
            questStats = new QuestStats();
            questStats.setQuestWeekStart(questData.getQuestsStartTimestamp());
        }

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
            powerupData.get("enhancer").setRaritiesUsed(new int[]{0,0,0,0,0,0,0});
        }

        if (powerupData.get("clone") == null) {
            powerupData.put("clone", new CollectedPowerup());
            powerupData.get("clone").setNumSuccess(0);
            powerupData.get("clone").setRaritiesUsed(new int[]{0,0,0,0,0,0,0,0,0,0});
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
            Map<String, RarityConfig> rarityConfigs = this.config.getRarityConfigs();
            boolean isSpecial = Arrays.asList(rarityConfigs.get("special").getBoars()).contains(boarID);

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

        BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
        writer.write(new Gson().toJson(this.data));
        writer.close();
    }

    public void updateUserData() throws IOException {
        BoarUserData userData = this.getUserData(false);

        QuestData questData = new QuestData();
        QuestStats questStats = this.data.getStats().getQuests();
        List<String> curQuestIDs = Arrays.asList(questData.getCurQuestIDs());

        int dailyQuestIndex = curQuestIDs.indexOf("daily");
        int cloneBoarsIndex = curQuestIDs.indexOf("cloneBoars");
        int cloneRarityIndex = curQuestIDs.indexOf("cloneRarity");
        int sendGiftsIndex = curQuestIDs.indexOf("sendGifts");
        int openGiftsIndex = curQuestIDs.indexOf("openGifts");
        int powParticipateIndex = curQuestIDs.indexOf("powParticipate");

        Map<String, CollectedPowerup> powerupData = this.data.getItemCollection().getPowerups();
        Map<String, CollectedPowerup> filePowerupData = userData.getItemCollection().getPowerups();
        GeneralStats genStats = this.data.getStats().getGeneral();
        GeneralStats fileGenStats = userData.getStats().getGeneral();

        if (dailyQuestIndex >= 0) {
            questStats.getProgress()[dailyQuestIndex] += genStats.getNumDailies() - fileGenStats.getNumDailies();
        }

        if (cloneBoarsIndex >= 0) {
            questStats.getProgress()[cloneBoarsIndex] += powerupData.get("clone").getNumSuccess() -
                filePowerupData.get("clone").getNumSuccess();
        }

        int[] cloneRarities = powerupData.get("clone").getRaritiesUsed();
        int[] fileCloneRarities = filePowerupData.get("clone").getRaritiesUsed();

        if (cloneRarityIndex >= 0) {
            questStats.getProgress()[cloneRarityIndex] += cloneRarities[cloneRarityIndex / 2 + 2] -
                fileCloneRarities[cloneRarityIndex / 2 + 2];
        }

        if (sendGiftsIndex >= 0) {
            questStats.getProgress()[sendGiftsIndex] += powerupData.get("gift").getNumUsed() -
                filePowerupData.get("gift").getNumUsed();
        }

        if (openGiftsIndex >= 0) {
            questStats.getProgress()[openGiftsIndex] += powerupData.get("gift").getNumOpened() -
                filePowerupData.get("gift").getNumOpened();
        }

        if (powParticipateIndex >= 0) {
            questStats.getProgress()[powParticipateIndex] += this.data.getStats().getPowerups().getAttempts() -
                userData.getStats().getPowerups().getAttempts();
        }

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

    public List<Integer> addBoars(List<String> boarIDs, List<Integer> scores) throws IOException {
        List<String> boarRarityKeys = new ArrayList<>();

        for (String boarID : boarIDs) {
            boarRarityKeys.add(BoarUtil.findRarityKey(boarID));
        }

        List<Integer> boarEditions = new ArrayList<>();
        List<Integer> firstEditions = new ArrayList<>();

        ItemsDataUtil.updateGlobalBoarData(boarIDs, boarRarityKeys, boarEditions, firstEditions);
        this.addBoarData(boarIDs, boarRarityKeys, scores, boarEditions);

        return firstEditions;
    }

    private synchronized void addBoarData(
        List<String> boarIDs, List<String> boarRarityKeys, List<Integer> scores, List<Integer> boarEditions
    ) throws IOException {
        NumberConfig nums = this.config.getNumberConfig();

        QuestsDataUtil questsDataUtil = new QuestsDataUtil();
        QuestData questData = questsDataUtil.getData();

        this.refreshUserData(false);

        GeneralStats genStats = this.data.getStats().getGeneral();

        for (int i=0; i<boarIDs.size(); i++) {
            int collectBoarIndex = Arrays.asList(questData.getCurQuestIDs()).indexOf("collectBoar");
            int collectBucksIndex = Arrays.asList(questData.getCurQuestIDs()).indexOf("collectBucks");

            String boarID = boarIDs.get(i);
            String boarRarityKey = boarRarityKeys.get(i);

            if (collectBucksIndex != -1) {
                this.data.getStats().getQuests().getProgress()[collectBucksIndex] += scores.get(i);
            }

            boolean commonMatch = collectBoarIndex / 2 == 0 && boarRarityKey.equals("common");
            boolean uncommonMatch = collectBoarIndex / 2 == 1 && boarRarityKey.equals("uncommon");
            boolean rareMatch = collectBoarIndex / 2 == 2 && boarRarityKey.equals("rare");
            boolean epicMatch = collectBoarIndex / 2 == 3 && boarRarityKey.equals("epic");

            if (collectBoarIndex != -1 && (commonMatch || uncommonMatch || rareMatch || epicMatch)) {
                this.data.getStats().getQuests().getProgress()[collectBoarIndex]++;
            }

            if (!this.data.getItemCollection().getBoars().containsKey(boarID)) {
                this.data.getItemCollection().getBoars().put(boarID, new CollectedBoar());
                this.data.getItemCollection().getBoars().get(boarID).setFirstObtained(
                    LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
                );
            }

            CollectedBoar boarToAdd = this.data.getItemCollection().getBoars().get(boarID);
            long curTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();

            boarToAdd.setNum(boarToAdd.getNum()+1);
            boarToAdd.setLastObtained(curTime);

            if (boarEditions.get(i) <= nums.getMaxTrackedEditions() || boarRarityKey.equals("special")) {
                boarToAdd.getEditions().add(boarEditions.get(i));
                boarToAdd.getEditions().sort(Integer::compareTo);

                boarToAdd.getEditionDates().add(curTime);
                boarToAdd.getEditionDates().sort(Long::compareTo);
            }

            genStats.setLastBoar(boarID);
            genStats.setBoarScore(genStats.getBoarScore() + scores.get(i));
        }

        genStats.setTotalBoars(genStats.getTotalBoars() + boarIDs.size());

        this.orderBoars();
        this.updateUserData();
    }

    private void orderBoars() {
        String[] boarIDs = this.data.getItemCollection().getBoars().keySet().toArray(new String[0]);
        RarityConfig[] rarities = this.config.getRarityConfigs().values().toArray(new RarityConfig[0]);

        for (int i=rarities.length-1; i>0; i--) {
            Set<String> orderedBoars = new HashSet<>();
            String[] boarsOfRarity = rarities[i].boars;

            for (int j=0; j<boarIDs.length; j++) {
                String curBoarID = boarIDs[j];
                CollectedBoar curBoarData = this.data.getItemCollection().getBoars().get(curBoarID);

                if (!Arrays.asList(boarsOfRarity).contains(curBoarID) || orderedBoars.contains(curBoarID)) {
                    continue;
                }

                this.data.getItemCollection().getBoars().remove(curBoarID);
                this.data.getItemCollection().getBoars().put(curBoarID, curBoarData);

                orderedBoars.add(curBoarID);
                j--;
            }
        }
    }

    public synchronized void incRefs() {
        this.numRefs++;
    }

    public synchronized void decRefs() {
        this.numRefs--;
    }
}
