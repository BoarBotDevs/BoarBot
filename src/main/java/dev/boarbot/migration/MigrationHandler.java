package dev.boarbot.migration;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.ConfigUpdater;
import dev.boarbot.migration.globaldata.UserMarketData;
import dev.boarbot.migration.guilddata.OldGuildData;
import dev.boarbot.migration.userdata.NewBoarData;
import dev.boarbot.migration.userdata.OldUserData;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.Log;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class MigrationHandler implements Configured {
    public static void doMigration() {
        try {
            if (DataUtil.databaseHasData()) {
                Log.info(MigrationHandler.class, "Skipping migration. Database already initialized");
                return;
            }
        } catch (SQLException exception) {
            Log.error(MigrationHandler.class, "Failed to check if database has data", exception);
            System.exit(-1);
        }

        try {
            ConfigUpdater.setMaintenance(true);
        } catch (IOException exception) {
            Log.error(MigrationHandler.class, "Failed to enable maintenance mode", exception);
            System.exit(-1);
        }

        Map<String, UserMarketData> userMarketData = MigrationReader.getUserMarketData();
        List<OldUserData> oldUsers = MigrationReader.getOldUsers(userMarketData);
        List<OldGuildData> oldGuildData = MigrationReader.getOldGuilds();
        Map<String, PriorityQueue<NewBoarData>> oldBoars = MigrationReader.getBoars(oldUsers, userMarketData);

        if (oldGuildData != null) {
            MigrationWriter.writeGuilds(oldGuildData);
        }

        if (oldUsers != null) {
            MigrationWriter.writeUsers(oldUsers);
        }

        if (oldBoars != null) {
            MigrationWriter.writeBoars(oldBoars);
        }
    }
}
