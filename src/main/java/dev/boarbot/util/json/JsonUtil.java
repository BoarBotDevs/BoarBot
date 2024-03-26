package dev.boarbot.util.json;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public final class JsonUtil {
    public static String pathToJson(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner reader = new Scanner(file);
        StringBuilder jsonStr = new StringBuilder();
        Gson g = new Gson();

        while(reader.hasNextLine()) {
            jsonStr.append(reader.nextLine());
        }

        return jsonStr.toString();
    }
}
