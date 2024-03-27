package dev.boarbot.util.data.types;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PowerupData {
    Map<String, String[]> messagesInfo = new HashMap<>();
    Map<String, Integer> failedServer = new HashMap<>();
}
