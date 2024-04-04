package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserData;
import dev.boarbot.util.data.types.ItemData;
import dev.boarbot.util.data.types.ItemsData;
import dev.boarbot.util.json.JsonUtil;
import dev.boarbot.util.test.TestUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ItemsDataUtilTest {
    @Test
    public void testRefreshData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File itemsFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getItemDataFileName()
        );

        ItemsDataUtil itemsData = new ItemsDataUtil();

        TestUtil.assertCreate(itemsFile, itemsData);

        TestUtil.assertInvalidFile(itemsFile, itemsData);
    }

    @Test
    public void testSaveData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        BotConfig config = boarBot.getConfig();
        PathConfig pathConfig = config.getPathConfig();

        File itemsFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getItemDataFileName()
        );

        ItemsDataUtil itemsData = new ItemsDataUtil();

        File validFile = new File("src/test/resources/test_files/global_data/valid_items.json");
        String validJson = JsonUtil.pathToJson(validFile.getPath());

        ItemsData validData = new Gson().fromJson(validJson, ItemsData.class);
        validJson = new Gson().toJson(validData);

        TestUtil.assertRightDataHandle(itemsFile, validFile, itemsData, validJson);

        File missingFile = new File("src/test/resources/test_files/global_data/missing_items.json");
        String missingJson = JsonUtil.pathToJson(missingFile.getPath());

        ItemsData missingData = new Gson().fromJson(missingJson, ItemsData.class);

        for (String powerupID : config.getItemConfig().getPowerups().keySet()) {
            missingData.getPowerups().putIfAbsent(powerupID, new ItemData());
        }

        String missingFixedJson = new Gson().toJson(missingData);

        TestUtil.assertRightDataHandle(itemsFile, missingFile, itemsData, missingFixedJson);

        File wrongFile = new File("src/test/resources/test_files/global_data/wrong_items.json");

        File wrongUserFile = new File("src/test/resources/test_files/user_data/test_user.json");
        File userFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getUserDataFolder() + "test_user.json"
        );

        Files.copy(wrongUserFile.toPath(), userFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        TestUtil.assertRightDataHandle(itemsFile, wrongFile, itemsData, validJson);

        String wrongUserJson = JsonUtil.pathToJson(wrongUserFile.getPath());
        BoarUserData wrongUserData = new Gson().fromJson(wrongUserJson, BoarUserData.class);

        assertEquals(
            wrongUserData.getStats().getGeneral().getBoarScore() + 5,
            new BoarUser("test_user").getData().getStats().getGeneral().getBoarScore(),
            "User data not updated properly"
        );

        File unorderedFile = new File("src/test/resources/test_files/global_data/unordered_items.json");

        TestUtil.assertRightDataHandle(itemsFile, unorderedFile, itemsData, validJson);
    }
}
