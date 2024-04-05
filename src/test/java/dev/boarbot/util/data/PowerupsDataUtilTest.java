package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.util.data.types.PowerupData;
import dev.boarbot.util.json.JsonUtil;
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

    @Test
    public void testSaveData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File powerupsFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getPowerupDataFileName()
        );

        PowerupsDataUtil powerupsData = new PowerupsDataUtil();

        File validFile = new File("src/test/resources/test_files/global_data/valid_powerups.json");
        String validJson = JsonUtil.pathToJson(validFile.getPath());

        PowerupData validData = new Gson().fromJson(validJson, PowerupData.class);
        validJson = new Gson().toJson(validData);

        TestUtil.assertRightDataHandle(powerupsFile, validFile, powerupsData, validJson);
    }
}
