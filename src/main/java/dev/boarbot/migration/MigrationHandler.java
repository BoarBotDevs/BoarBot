package dev.boarbot.migration;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.ConfigUpdater;
import dev.boarbot.migration.userdata.OldUserData;
import dev.boarbot.util.logging.Log;

import java.io.IOException;
import java.util.List;

public class MigrationHandler implements Configured {
    public static void doMigration() {
        List<OldUserData> oldUsers = MigrationReader.getOldUsers();

        boolean priorMaintenance = CONFIG.getMainConfig().isMaintenanceMode();

        if (!priorMaintenance) {
            try {
                ConfigUpdater.toggleMaintenance();
            } catch (IOException exception) {
                Log.error(MigrationHandler.class, "Failed to enable maintenance mode", exception);
                System.exit(-1);
            }
        }

        MigrationWriter.writeGuilds(MigrationReader.getOldGuilds());
        MigrationWriter.writeUsers(oldUsers);
        MigrationWriter.writeBoars(MigrationReader.getBoars(oldUsers));

        if (!priorMaintenance) {
            try {
                ConfigUpdater.toggleMaintenance();
            } catch (IOException exception) {
                Log.error(MigrationHandler.class, "Failed to disable maintenance mode", exception);
            }
        }
    }
}
