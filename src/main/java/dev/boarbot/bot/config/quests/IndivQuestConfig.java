package dev.boarbot.bot.config.quests;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IndivQuestConfig {
    private String requirement = "";
    private String rewardType = "";
    private int rewardAmt = 0;
}
