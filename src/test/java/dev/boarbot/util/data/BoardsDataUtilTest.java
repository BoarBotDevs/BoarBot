package dev.boarbot.util.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.bot.config.commands.ArgChoicesConfig;
import dev.boarbot.util.data.types.BoardData;
import dev.boarbot.util.json.JsonUtil;
import dev.boarbot.util.test.TestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class BoardsDataUtilTest {
    @Test
    public void testRefreshData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        PathConfig pathConfig = boarBot.getConfig().getPathConfig();

        File boardsFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getLeaderboardsFileName()
        );

        BoardsDataUtil boardData = new BoardsDataUtil();

        TestUtil.assertCreate(boardsFile, boardData);

        TestUtil.assertInvalidFile(boardsFile, boardData);
    }

    @Test
    public void testSaveData() throws IOException {
        Bot boarBot = TestUtil.getBot();

        BotConfig config = boarBot.getConfig();
        PathConfig pathConfig = config.getPathConfig();

        File boardFile = new File(
            pathConfig.getDatabaseFolder() + pathConfig.getGlobalDataFolder() + pathConfig.getLeaderboardsFileName()
        );

        BoardsDataUtil boardData = new BoardsDataUtil();

        Type mapType = new TypeToken<Map<String, BoardData>>() {}.getType();

        File validFile = new File("src/test/resources/test_files/global_data/valid_board.json");
        String validJson = JsonUtil.pathToJson(validFile.getPath());

        Map<String, Long> validData = new Gson().fromJson(validJson, mapType);
        validJson = new Gson().toJson(validData);

        TestUtil.assertRightDataHandle(boardFile, validFile, boardData, validJson);

        ArgChoicesConfig<?>[] choices = config.getCommandConfig()
            .get("boar").getSubcommands().get("top").getOptions()[0].getChoices();

        File missingFile = new File("src/test/resources/test_files/global_data/missing_board.json");
        String missingJson = JsonUtil.pathToJson(missingFile.getPath());

        Map<String, BoardData> missingData = new Gson().fromJson(missingJson, mapType);

        for (ArgChoicesConfig<?> choice : choices) {
            String boardID = choice.getValue().toString();
            missingData.putIfAbsent(boardID, new BoardData());
        }

        String missingFixedJson = new Gson().toJson(missingData);

        TestUtil.assertRightDataHandle(boardFile, missingFile, boardData, missingFixedJson);

        List<?> choiceValues = Arrays.stream(choices).map(ArgChoicesConfig::getValue).toList();

        File wrongFile = new File("src/test/resources/test_files/global_data/wrong_board.json");
        String wrongJson = JsonUtil.pathToJson(wrongFile.getPath());

        Map<String, BoardData> wrongData = new Gson().fromJson(wrongJson, mapType);

        for (String boardID : new HashSet<>(wrongData.keySet())) {
            if (!choiceValues.contains(boardID)) {
                wrongData.remove(boardID);
            }
        }

        String wrongFixedJson = new Gson().toJson(wrongData);

        TestUtil.assertRightDataHandle(boardFile, wrongFile, boardData, wrongFixedJson);
    }
}
