package dev.boarbot.migration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.BoarBotApp;
import dev.boarbot.migration.globaldata.OldBoardData;
import dev.boarbot.migration.guilddata.OldGuildData;
import dev.boarbot.migration.userdata.BoarData;
import dev.boarbot.migration.userdata.NewBoarData;
import dev.boarbot.migration.userdata.OldUserData;
import dev.boarbot.util.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class MigrationReader {
    private final static Path oldUsersPath = Paths.get("database/users/");
    private final static Path oldLeaderboardPath = Paths.get("database/global/leaderboards.json");
    private final static Path oldGuildsPath = Paths.get("database/guilds/");

    public static List<OldUserData> getOldUsers() {
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

    public static Map<String, PriorityQueue<NewBoarData>> getBoars(List<OldUserData> oldUsers) {
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
                        : 0;
                    boars.get(boarID).add(new NewBoarData(oldUser.getUserID(), editionDate));
                }
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
}
