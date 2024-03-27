package dev.boarbot.util.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.bot.config.commands.ArgChoicesConfig;
import dev.boarbot.util.data.types.BoardData;
import dev.boarbot.util.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class BoardsDataUtil extends DataUtil {
    private Map<String, BoardData> data;

    public BoardsDataUtil() {
        this(false);
    }

    public BoardsDataUtil(boolean update) {
        this.filePath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder() +
            this.pathConfig.getLeaderboardsFileName();
        this.data = refreshData(update);
    }

    public Map<String, BoardData> refreshData(boolean update) {
        createGlobalFolder();

        this.data = new HashMap<>();

        Type mapType = new TypeToken<Map<String, BoardData>>() {}.getType();
        String dataJson = null;

        try {
            dataJson = JsonUtil.pathToJson(this.filePath);
        } catch (FileNotFoundException e) {
            log.info("Unable to find file at %s. Attempting to create.".formatted(this.filePath));
        }

        if (dataJson == null) {
            ArgChoicesConfig<?>[] choices = this.config.getCommandConfig()
                .get("boar").getSubcommands().get("top").getOptions()[0].choices;

            for (ArgChoicesConfig<?> choice : choices) {
                String boardID = (String) choice.value;
                this.data.put(boardID, new BoardData());
            }

            dataJson = createFile(this.filePath, this.data);
        }

        this.data = new Gson().fromJson(dataJson, mapType);

        if (update) {
            updateData();
        }

        return this.data;
    }

    private void updateData() {
        ArgChoicesConfig<?>[] choices = this.config.getCommandConfig()
            .get("boar").getSubcommands().get("top").getOptions()[0].choices;
        String[] choiceValues = (String[]) Arrays.stream(choices).map(choice -> choice.value).toArray();

        for (ArgChoicesConfig<?> choice : choices) {
            String boardID = (String) choice.value;
            this.data.putIfAbsent(boardID, new BoardData());
        }

        for (String boardID : this.data.keySet()) {
            if (Arrays.asList(choiceValues).contains(boardID)) {
                this.data.remove(boardID);
            }
        }

        try {
            saveData();
        } catch (IOException exception) {
            log.error("Failed to update file %s.".formatted(filePath), exception);
            System.exit(-1);
        }
    }

    @Override
    public Map<String, BoardData> getData() {
        return this.data;
    }

    @Override
    public void saveData() throws IOException {
        saveData(this.filePath, this.data);
    }
}
