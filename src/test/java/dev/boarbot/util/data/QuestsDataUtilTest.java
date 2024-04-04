package dev.boarbot.util.data;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.util.test.TestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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
}
