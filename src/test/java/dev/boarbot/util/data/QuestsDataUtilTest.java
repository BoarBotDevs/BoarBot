package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.util.data.types.QuestData;
import dev.boarbot.util.json.JsonUtil;
import dev.boarbot.util.test.TestUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class QuestsDataUtilTest {
    @Test
    public void testRefreshData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File questFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getQuestDataFileName()
        );

        QuestsDataUtil questData = new QuestsDataUtil();

        TestUtil.assertCreate(questFile, questData);
        TestUtil.assertInvalidFile(questFile, questData);
    }

    @Test
    public void testSaveData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File questFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getQuestDataFileName()
        );

        questFile.delete();

        QuestsDataUtil questData = new QuestsDataUtil(true);

        assertTrue(questData.getData().getQuestsStartTimestamp() > 0, "Timestamp not set");

        for (String questID : questData.getData().getCurQuestIDs()) {
            assertFalse(questID.isEmpty(), "One or more quest IDs not set");
        }

        File validFile = new File("src/test/resources/test_files/global_data/valid_quests.json");
        String validJson = JsonUtil.pathToJson(validFile.getPath());

        QuestData validData = new Gson().fromJson(validJson, QuestData.class);
        validJson = new Gson().toJson(validData);

        Files.copy(validFile.toPath(), questFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        questData.refreshData(true);

        String dataJson = new Gson().toJson(questData.getData());

        assertNotEquals(validJson, dataJson);
        questFile.delete();
    }
}
