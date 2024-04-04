package dev.boarbot.util.test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.boarbot.BoarBotApp;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.util.data.DataUtil;
import lombok.extern.log4j.Log4j2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Log4j2
public final class TestUtil {
    public static Bot getBot() {
        BoarBotApp.reset();
        return BoarBotApp.getBot();
    }

    public static void assertCreate(File dataFile, DataUtil dataUtil) {
        dataFile.delete();
        dataUtil.refreshData(false);
        assertTrue(dataFile.isFile());
    }

    public static void assertInvalidFile(File dataFile, DataUtil dataUtil) throws IOException {
        File invalidFile = new File("src/test/resources/test_files/global_data/invalid.json");
        Files.copy(invalidFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        assertThrowsExactly(JsonSyntaxException.class, () -> dataUtil.refreshData(false));
        dataFile.delete();
    }

    public static void assertRightDataHandle(File dataFile, File inputFile, DataUtil dataUtil, String expected) throws IOException {
        Files.copy(inputFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        dataUtil.refreshData(true);

        String dataJson = new Gson().toJson(dataUtil.getData());

        assertEquals(expected, dataJson);
        dataFile.delete();
    }
}
