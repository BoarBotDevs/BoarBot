package dev.boarbot.util.data;

import com.google.gson.Gson;
import dev.boarbot.util.data.types.QuestData;
import dev.boarbot.util.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.Set;

@Log4j2
public class QuestsDataUtil extends DataUtil {
    private QuestData data;

    public QuestsDataUtil() {
        this(false);
    }

    public QuestsDataUtil(boolean update) {
        this.filePath = this.pathConfig.getDatabaseFolder() + this.pathConfig.getGlobalDataFolder() +
            this.pathConfig.getQuestDataFileName();
        this.data = refreshData(update);
    }

    public QuestData refreshData(boolean update) {
        createGlobalFolder();

        this.data = new QuestData();

        String dataJson = null;

        try {
            dataJson = JsonUtil.pathToJson(this.filePath);
        } catch (FileNotFoundException e) {
            log.info("Unable to find file at %s. Attempting to create.".formatted(this.filePath));
        }

        if (dataJson == null) {
            dataJson = createFile(this.filePath, this.data);
        }

        this.data = new Gson().fromJson(dataJson, QuestData.class);

        if (update) {
            updateData();
        }

        return this.data;
    }

    private void updateData() {
        long curTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        int oneDay = this.config.getNumberConfig().getOneDay();

        if (this.data.getQuestsStartTimestamp() + oneDay * 7L < curTime) {
            Set<String> questIDs = this.config.getQuestConfig().keySet();

            LocalDateTime date = Instant.ofEpochMilli(curTime + 60000).atOffset(ZoneOffset.UTC).toLocalDateTime();
            int dayOfWeekNum = date.get(WeekFields.SUNDAY_START.dayOfWeek()) - 1;
            long startOfDay = date.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();

            this.data.setQuestsStartTimestamp(startOfDay - (long) dayOfWeekNum * oneDay);

            for (int i=0; i<this.data.getCurQuestIDs().length; i++) {
                int chosenIndex = (int) Math.floor(Math.random() * questIDs.size());
                int curIndex = 0;

                for (String questID : questIDs) {
                    if (curIndex == chosenIndex) {
                        this.data.getCurQuestIDs()[i] = questID;
                        questIDs.remove(questID);
                        break;
                    }
                    curIndex++;
                }
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
    public QuestData getData() {
        return this.data;
    }

    @Override
    public void saveData() throws IOException {
        saveData(this.filePath, this.data);
    }
}
