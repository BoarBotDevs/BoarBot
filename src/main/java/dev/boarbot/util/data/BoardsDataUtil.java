package dev.boarbot.util.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.bot.config.commands.ArgChoicesConfig;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.collectibles.CollectedBoar;
import dev.boarbot.entities.boaruser.collectibles.CollectedItems;
import dev.boarbot.entities.boaruser.stats.GeneralStats;
import dev.boarbot.entities.boaruser.stats.PowerupStats;
import dev.boarbot.util.data.types.BoardData;
import dev.boarbot.util.data.types.UserBoardData;
import dev.boarbot.util.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

@Log4j2
public class BoardsDataUtil extends DataUtil {
    private Map<String, BoardData> data;

    public BoardsDataUtil() {
        this(false);
    }

    public BoardsDataUtil(boolean update) {
        this.filePath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder() +
            this.pathConfig.getLeaderboardsFileName();
        this.data = refreshData(update);
    }

    @Override
    public Map<String, BoardData> refreshData(boolean update) {
        createGlobalFolder();

        this.data = new HashMap<>();

        Type mapType = new TypeToken<Map<String, BoardData>>() {}.getType();
        String dataJson = null;

        try {
            dataJson = JsonUtil.pathToJson(this.filePath);
        } catch (FileNotFoundException e) {
            log.info("Unable to find file at %s. Attempting to create.".formatted(this.filePath));
        }

        if (dataJson == null) {
            ArgChoicesConfig<?>[] choices = this.config.getCommandConfig()
                .get("boar").getSubcommands().get("top").getOptions()[0].getChoices();

            for (ArgChoicesConfig<?> choice : choices) {
                String boardID = (String) choice.getValue();
                this.data.put(boardID, new BoardData());
            }

            dataJson = createFile(this.filePath, this.data);
        }

        this.data = new Gson().fromJson(dataJson, mapType);

        if (update) {
            updateData();
        }

        return this.data;
    }

    private void updateData() {
        ArgChoicesConfig<?>[] choices = this.config.getCommandConfig()
            .get("boar").getSubcommands().get("top").getOptions()[0].getChoices();
        List<?> choiceValues = Arrays.stream(choices).map(ArgChoicesConfig::getValue).toList();

        for (ArgChoicesConfig<?> choice : choices) {
            String boardID = choice.getValue().toString();
            this.data.putIfAbsent(boardID, new BoardData());
        }

        for (String boardID : new HashSet<>(this.data.keySet())) {
            if (!choiceValues.contains(boardID)) {
                this.data.remove(boardID);
            }
        }

        try {
            saveData();
        } catch (IOException exception) {
            log.error("Failed to update file %s.".formatted(filePath), exception);
            System.exit(-1);
        }
    }

    public void updateUserData(BoarUser boarUser) throws IOException {
        String userID = boarUser.getUserID();
        String username = boarUser.getUser().getName();

        GeneralStats genStats = boarUser.getData().getStats().getGeneral();
        PowerupStats powerupStats = boarUser.getData().getStats().getPowerups();
        CollectedItems itemCollection = boarUser.getData().getItemCollection();

        UserBoardData userData = new UserBoardData();
        userData.setUsername(username);

        if (genStats.getBoarScore() > 0) {
            userData.setValue(genStats.getBoarScore());
            this.data.get("bucks").getUserData().put(userID, userData);
        } else {
            this.data.get("bucks").getUserData().remove(userID);
        }

        if (genStats.getTotalBoars() > 0) {
            userData.setValue((long) genStats.getTotalBoars());
            this.data.get("total").getUserData().put(userID, userData);
        } else {
            this.data.get("total").getUserData().remove(userID);
        }

        int uniques = 0;
        int sbUniques = 0;

        for (String boarID : itemCollection.getBoars().keySet()) {
            CollectedBoar boarData = itemCollection.getBoars().get(boarID);
            IndivItemConfig boarInfo = this.config.getItemConfig().getBoars().get(boarID);

            if (boarData.getNum() > 0 && !boarInfo.getIsSB()) {
                uniques++;
            } else if (boarData.getNum() > 0) {
                sbUniques++;
            }
        }

        if (uniques > 0) {
            userData.setValue((long) uniques);
            this.data.get("uniques").getUserData().put(userID, userData);
        } else {
            this.data.get("uniques").getUserData().remove(userID);
        }

        if (uniques + sbUniques > 0) {
            userData.setValue((long) uniques + sbUniques);
            this.data.get("uniquesSB").getUserData().put(userID, userData);
        } else {
            this.data.get("uniquesSB").getUserData().remove(userID);
        }

        if (genStats.getBoarStreak() > 0) {
            userData.setValue((long) genStats.getBoarStreak());
            this.data.get("streak").getUserData().put(userID, userData);
        } else {
            this.data.get("streak").getUserData().remove(userID);
        }

        if (powerupStats.getAttempts() > 0) {
            userData.setValue((long) powerupStats.getAttempts());
            this.data.get("attempts").getUserData().put(userID, userData);
        } else {
            this.data.get("attempts").getUserData().remove(userID);
        }

        if (powerupStats.getOneAttempts() > 0) {
            userData.setValue((long) powerupStats.getOneAttempts());
            this.data.get("topAttempts").getUserData().put(userID, userData);
        } else {
            this.data.get("topAttempts").getUserData().remove(userID);
        }

        if (itemCollection.getPowerups().get("gift").getNumUsed() > 0) {
            userData.setValue((long) itemCollection.getPowerups().get("gift").getNumUsed());
            this.data.get("giftsUsed").getUserData().put(userID, userData);
        } else {
            this.data.get("giftsUsed").getUserData().remove(userID);
        }

        long multiplier = genStats.getMultiplier() + 1;

        for (int i=0; i<itemCollection.getPowerups().get("miracle").getNumActive(); i++) {
            multiplier += (long) Math.min(
                Math.ceil(multiplier * 0.1), this.config.getNumberConfig().getMiracleIncreaseMax()
            );
        }

        multiplier--;

        if (multiplier > 0) {
            userData.setValue(multiplier);
            this.data.get("multiplier").getUserData().put(userID, userData);
        } else {
            this.data.get("multiplier").getUserData().remove(userID);
        }

        if (powerupStats.getFastestTime() > 0) {
            userData.setValue((long) powerupStats.getFastestTime());
            this.data.get("fastest").getUserData().put(userID, userData);
        } else {
            this.data.get("fastest").getUserData().remove(userID);
        }

        saveData();
    }

    public void removeUser(String userID) throws IOException {
        for (BoardData board : this.data.values()) {
            board.getUserData().remove(userID);

            if (board.getTopUser().equals(userID)) {
                board.setTopUser(null);
            }
        }

        saveData();
    }

    @Override
    public Map<String, BoardData> getData() {
        return this.data;
    }

    @Override
    public void saveData() throws IOException {
        saveData(this.filePath, this.data);
    }
}
