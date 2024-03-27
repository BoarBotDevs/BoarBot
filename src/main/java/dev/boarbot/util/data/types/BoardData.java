package dev.boarbot.util.data.types;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class BoardData {
    private String topUser;
    private Map<String, UserBoardData> userData = new HashMap<>();
}
