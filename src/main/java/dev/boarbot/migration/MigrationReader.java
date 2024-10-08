package dev.boarbot.migration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.migration.globaldata.*;
import dev.boarbot.migration.guilddata.OldGuildData;
import dev.boarbot.migration.userdata.*;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class MigrationReader implements Configured {
    private final static Path oldUsersPath = Paths.get("database/users/");
    private final static Path oldLeaderboardPath = Paths.get("database/global/leaderboards.json");
    private final static Path oldGuildsPath = Paths.get("database/guilds/");
    private final static Path oldMarketPath = Paths.get("database/global/items.json");

    public static List<OldUserData> getOldUsers(Map<String, UserMarketData> userMarketData) {
        Log.debug(MigrationReader.class, "Getting old user data...");

        List<OldUserData> oldUsers = new ArrayList<>();
        File[] oldUserFiles = oldUsersPath.toFile().listFiles();

        if (oldUserFiles == null) {
            return oldUsers;
        }

        Gson g = new Gson();
        Map<String, String> usernameMap = getBoardUserMap();

        for (File file : oldUserFiles) {
            try {
                OldUserData oldUser = g.fromJson(new FileReader(file), OldUserData.class);

                oldUser.setUserID(file.getName().split("\\.")[0]);

                if (usernameMap.containsKey(oldUser.getUserID())) {
                    oldUser.setUsername(usernameMap.get(oldUser.getUserID()));
                } else {
                    oldUser.setUsername(
                        BoarBotApp.getBot().getJDA().retrieveUserById(oldUser.getUserID()).complete().getName()
                    );
                }

                if (userMarketData.containsKey(oldUser.getUserID())) {
                    UserMarketData curUserMarketData = userMarketData.get(oldUser.getUserID());

                    GeneralStatsData generalStatsData = oldUser.getStats().getGeneral();
                    generalStatsData.setBoarScore(generalStatsData.getBoarScore() + curUserMarketData.getBucks());

                    for (int i=0; i<curUserMarketData.getItemIDs().size(); i++) {
                        String itemID = curUserMarketData.getItemIDs().get(i);
                        boolean isPowerup = POWS.containsKey(itemID) || itemID.equals("enhancer");

                        if (!isPowerup) {
                            continue;
                        }

                        PowerupData curPowerup = oldUser.getItemCollection().getPowerups().get(itemID);

                        if (itemID.equals("enhancer") && curPowerup.getNumTotal() == NUMS.getMaxTransmute()) {
                            continue;
                        }

                        curPowerup.setNumTotal(curPowerup.getNumTotal() + 1);
                    }
                }

                oldUsers.add(oldUser);
            } catch (FileNotFoundException exception) {
                Log.error(MigrationReader.class, "Failed to find file: " + file.toPath(), exception);
            }
        }

        Log.debug(MigrationReader.class, "Obtained all old user data");
        return oldUsers;
    }

    private static Map<String, String> getBoardUserMap() {
        Map<String, String> usernameMap = new HashMap<>();
        File oldLeaderboardFile = oldLeaderboardPath.toFile();

        if (oldLeaderboardFile.exists()) {
            try {
                Map<String, OldBoardData> boards = new Gson().fromJson(
                    new FileReader(oldLeaderboardFile), new TypeToken<Map<String, OldBoardData>>(){}.getType()
                );

                for (OldBoardData boardData : boards.values()) {
                    for (String userID : boardData.getUserData().keySet()) {
                        usernameMap.put(userID, boardData.getUserData().get(userID).get(0).getAsString());
                    }
                }
            } catch (FileNotFoundException exception) {
                Log.error(MigrationReader.class, "Failed to find leaderboard data", exception);
            }
        }

        return usernameMap;
    }

    public static Map<String, PriorityQueue<NewBoarData>> getBoars(
        List<OldUserData> oldUsers, Map<String, UserMarketData> userMarketData
    ) {
        Log.debug(MigrationReader.class, "Getting old boar data...");

        Map<String, PriorityQueue<NewBoarData>> boars = new HashMap<>();

        for (OldUserData oldUser : oldUsers) {
            for (String boarID : oldUser.getItemCollection().getBoars().keySet()) {
                BoarData boar = oldUser.getItemCollection().getBoars().get(boarID);
                int boarNum = boar.getNum();

                if (boarNum == 0) {
                    continue;
                }

                if (!boars.containsKey(boarID)) {
                    boars.put(boarID, new PriorityQueue<>());
                }

                for (int i=0; i<boarNum; i++) {
                    long editionDate = i < boar.getEditionDates().length
                        ? boar.getEditionDates()[i]
                        : Long.MAX_VALUE;
                    boars.get(boarID).add(new NewBoarData(oldUser.getUserID(), editionDate));
                }
            }
        }

        for (String userID : userMarketData.keySet()) {
            UserMarketData curUserMarketData = userMarketData.get(userID);
            int numCharges = 0;

            for (int i=0; i<curUserMarketData.getItemIDs().size(); i++) {
                String itemID = curUserMarketData.getItemIDs().get(i);
                boolean isPowerup = POWS.containsKey(itemID);
                boolean isTransmute = itemID.equals("enhancer");

                if (isTransmute) {
                    numCharges++;
                    continue;
                }

                if (isPowerup) {
                    continue;
                }

                if (!boars.containsKey(itemID)) {
                    boars.put(itemID, new PriorityQueue<>());
                }

                boars.get(itemID).add(new NewBoarData(userID, curUserMarketData.getEditionDates().get(i)));
            }

            List<String> convertedBoars = new ArrayList<>();
            String curRarity = "common";

            while (numCharges >= RARITIES.get("common").getChargesNeeded()) {
                if (numCharges < RARITIES.get(curRarity).getChargesNeeded()) {
                    convertedBoars.add(BoarUtil.findValid(curRarity, false));
                    curRarity = "common";
                } else if (curRarity.equals("divine")) {
                    convertedBoars.add(BoarUtil.findValid("immaculate", false));
                    numCharges -= RARITIES.get(curRarity).getChargesNeeded();
                    curRarity = "common";
                } else {
                    numCharges -= RARITIES.get(curRarity).getChargesNeeded();
                    curRarity = BoarUtil.getNextRarityKey(curRarity);
                }
            }

            if (!curRarity.equals("common")) {
                convertedBoars.add(BoarUtil.findValid(curRarity, false));
            }

            for (String boarID : convertedBoars) {
                if (!boars.containsKey(boarID)) {
                    boars.put(boarID, new PriorityQueue<>());
                }

                boars.get(boarID).add(new NewBoarData(userID, Long.MAX_VALUE));
            }
        }

        Log.debug(MigrationReader.class, "Obtained all old boar data");
        return boars;
    }

    public static List<OldGuildData> getOldGuilds() {
        Log.debug(MigrationReader.class, "Getting old guild data...");

        List<OldGuildData> oldGuilds = new ArrayList<>();
        File[] oldGuildFiles = oldGuildsPath.toFile().listFiles();

        if (oldGuildFiles == null) {
            return oldGuilds;
        }

        Gson g = new Gson();

        for (File file : oldGuildFiles) {
            try {
                OldGuildData oldGuild = g.fromJson(new FileReader(file), OldGuildData.class);

                oldGuild.setGuildID(file.getName().split("\\.")[0]);
                oldGuilds.add(oldGuild);
            } catch (FileNotFoundException exception) {
                Log.error(MigrationReader.class, "Failed to find file: " + file.toPath(), exception);
            }
        }

        Log.debug(MigrationReader.class, "Obtained all old guild data...");
        return oldGuilds;
    }

    public static Map<String, UserMarketData> getUserMarketData() {
        Log.debug(MigrationReader.class, "Getting old market data...");

        Map<String, UserMarketData> userMarketData = new HashMap<>();
        File oldMarketFile = oldMarketPath.toFile();

        if (oldMarketFile.exists()) {
            try {
                OldMarketData oldMarketData = new Gson().fromJson(new FileReader(oldMarketFile), OldMarketData.class);
                addBuySellData(userMarketData, oldMarketData.getPowerups());
                addBuySellData(userMarketData, oldMarketData.getBoars());
            } catch (FileNotFoundException exception) {
                Log.error(MigrationReader.class, "Failed to find old market data", exception);
            }
        }

        Log.debug(MigrationReader.class, "Obtained all old market data...");
        return userMarketData;
    }

    private static void addBuySellData(
        Map<String, UserMarketData> userMarketData, Map<String, OldItemData> oldItemData
    ) {
        for (String itemID : oldItemData.keySet()) {
            for (OldBuySellData buyData : oldItemData.get(itemID).getBuyers()) {
                String userID = buyData.getUserID();
                UserMarketData curUserMarketData = userMarketData.containsKey(userID)
                    ? userMarketData.get(userID)
                    : new UserMarketData();

                curUserMarketData.setBucks(
                    curUserMarketData.getBucks() + buyData.getPrice() * (buyData.getNum() - buyData.getFilledAmount())
                );

                for (int i=0; i<buyData.getFilledAmount()-buyData.getClaimedAmount(); i++) {
                    curUserMarketData.getItemIDs().add(itemID);
                    curUserMarketData.getEditionDates().add(
                        buyData.getEditionDates().length > i ? buyData.getEditionDates()[i] : Long.MAX_VALUE
                    );
                }

                userMarketData.put(userID, curUserMarketData);
            }

            for (OldBuySellData sellData : oldItemData.get(itemID).getSellers()) {
                String userID = sellData.getUserID();
                UserMarketData curUserMarketData = userMarketData.containsKey(userID)
                    ? userMarketData.get(userID)
                    : new UserMarketData();

                curUserMarketData.setBucks(
                    curUserMarketData.getBucks() + sellData.getPrice() *
                        (sellData.getFilledAmount() - sellData.getClaimedAmount())
                );

                for (int i=0; i<sellData.getNum()-sellData.getFilledAmount(); i++) {
                    curUserMarketData.getItemIDs().add(itemID);
                    curUserMarketData.getEditionDates().add(
                        sellData.getEditionDates().length > i ? sellData.getEditionDates()[i] : Long.MAX_VALUE
                    );
                }

                userMarketData.put(userID, curUserMarketData);
            }
        }
    }
}
