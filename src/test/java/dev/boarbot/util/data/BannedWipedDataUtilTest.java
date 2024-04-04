package dev.boarbot.util.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.util.json.JsonUtil;
import dev.boarbot.util.test.TestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class BannedWipedDataUtilTest {
    @Test
    public void testRefreshData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File banFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getBannedUsersFileName()
        );
        File wipeFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getWipeUsersFileName()
        );

        BannedWipedDataUtil banData = new BannedWipedDataUtil(true);
        BannedWipedDataUtil wipeData = new BannedWipedDataUtil(false);

        TestUtil.assertCreate(banFile, banData);
        TestUtil.assertCreate(wipeFile, wipeData);

        TestUtil.assertInvalidFile(banFile, banData);
        TestUtil.assertInvalidFile(wipeFile, wipeData);
    }

    @Test
    public void testSaveData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File banFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getBannedUsersFileName()
        );
        File wipeFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getWipeUsersFileName()
        );

        BannedWipedDataUtil banData = new BannedWipedDataUtil(true);
        BannedWipedDataUtil wipeData = new BannedWipedDataUtil(false);

        Gson g = new Gson();
        Type mapType = new TypeToken<Map<String, Long>>() {}.getType();

        File oneUserFile = new File("src/test/resources/test_files/global_data/banwipe_one_user.json");
        String oneUserJson = JsonUtil.pathToJson(oneUserFile.getPath());

        Map<String, Long> oneUserData = g.fromJson(oneUserJson, mapType);
        oneUserJson = g.toJson(oneUserData);

        TestUtil.assertRightDataHandle(banFile, oneUserFile, banData, oneUserJson);
        TestUtil.assertRightDataHandle(wipeFile, oneUserFile, wipeData, oneUserJson);

        File multiUserFile = new File("src/test/resources/test_files/global_data/banwipe_multi_users.json");
        String multiUserJson = JsonUtil.pathToJson(multiUserFile.getPath());

        Map<String, Long> multiUserData = g.fromJson(multiUserJson, mapType);
        multiUserJson = g.toJson(multiUserData);

        TestUtil.assertRightDataHandle(banFile, multiUserFile, banData, multiUserJson);
        TestUtil.assertRightDataHandle(wipeFile, multiUserFile, wipeData, multiUserJson);
    }
}
