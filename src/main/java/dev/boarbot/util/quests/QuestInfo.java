package dev.boarbot.util.quests;

import dev.boarbot.bot.config.quests.IndivQuestConfig;

import java.util.List;

public record QuestInfo(List<IndivQuestConfig> quests, boolean gaveBonus) {}
