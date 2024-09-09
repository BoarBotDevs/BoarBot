package dev.boarbot.bot.config.quests;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link QuestConfig QuestConfig.java}
 *
 * Stores a quest configuration for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class QuestConfig {
    private String description = "";
    private String descriptionAlt = "";
    private boolean disabled = false;
    private IndivQuestConfig[] questVals = {};
}
