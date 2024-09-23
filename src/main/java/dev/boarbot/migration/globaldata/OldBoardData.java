package dev.boarbot.migration.globaldata;

import com.google.gson.JsonArray;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class OldBoardData {
    private Map<String, JsonArray> userData = new HashMap<>();
}
