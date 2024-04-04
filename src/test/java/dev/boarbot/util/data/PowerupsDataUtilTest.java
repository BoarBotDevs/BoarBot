package dev.boarbot.util.data;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.util.test.TestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class PowerupsDataUtilTest {
    @Test
    public void testRefreshData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File powerupsFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getPowerupDataFileName()
        );

        PowerupsDataUtil powerupsData = new PowerupsDataUtil();

        TestUtil.assertCreate(powerupsFile, powerupsData);

        TestUtil.assertInvalidFile(powerupsFile, powerupsData);
    }
}
