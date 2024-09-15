package dev.boarbot.util.quests;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.quests.IndivQuestConfig;
import dev.boarbot.util.generators.EmbedImageGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class QuestUtil implements Configured {
    public static void sendQuestClaimMessage(InteractionHook hook, QuestInfo... questInfos) {
        List<QuestInfo> questConfigList = Arrays.stream(questInfos).toList();
        sendQuestClaimMessage(hook, questConfigList);
    }

    public static void sendQuestClaimMessage(InteractionHook hook, List<QuestInfo> questInfos) {
        String claimMessage = getQuestClaimMessage(questInfos);

        if (claimMessage == null) {
            return;
        }

        EmbedImageGenerator embedImageGenerator = new EmbedImageGenerator(claimMessage);

        try {
            MessageCreateBuilder msg = new MessageCreateBuilder()
                .setFiles(embedImageGenerator.generate().getFileUpload());
            hook.sendMessage(msg.build()).setEphemeral(true).queue();
        } catch (IOException exception) {
            log.error("Failed to send quest claim message", exception);
        }
    }

    private static String getQuestClaimMessage(List<QuestInfo> questInfos) {
        List<IndivQuestConfig> validQuests = new ArrayList<>();
        boolean claimedBonus = false;

        for (QuestInfo questInfo : questInfos) {
            if (questInfo == null) {
                continue;
            }

            for (IndivQuestConfig quest : questInfo.quests()) {
                claimedBonus = claimedBonus || questInfo.gaveBonus();
                if (quest != null) {
                    validQuests.add(quest);
                }
            }

        }

        if (validQuests.isEmpty()) {
            return null;
        }

        String claimString = validQuests.size() == 1
            ? "You received %s for completing a quest!"
            : "You received %s for completing multiple quests!";

        if (claimedBonus) {
            claimString += " Here's <>powerup<>3 Transmutation Charges<>font<> for perfecting this week!";
        }

        StringBuilder claimAmts = new StringBuilder();
        claimAmts.append(getRewardStr(validQuests.getFirst()));

        for (int i=1; i<validQuests.size(); i++) {
            IndivQuestConfig questConfig = validQuests.get(i);

            if (i != validQuests.size()-1) {
                claimAmts.append(", %s".formatted(getRewardStr(questConfig)));
            }

            if (i == validQuests.size()-1 && i == 1) {
                claimAmts.append(" and %s".formatted(getRewardStr(questConfig)));
            }

            if (i == validQuests.size()-1 && i > 1) {
                claimAmts.append(", and %s".formatted(getRewardStr(questConfig)));
            }
        }

        return claimString.formatted(claimAmts);
    }

    private static String getRewardStr(IndivQuestConfig questConfig) {
        String rewardType = questConfig.getRewardType();
        int rewardAmt = questConfig.getRewardAmt();

        if (rewardType.equals("bucks")) {
            return "<>bucks<>$%,d<>font<>".formatted(rewardAmt);
        }

        String powerupStr = rewardAmt == 1
            ? POWS.get(rewardType).getName()
            : POWS.get(rewardType).getPluralName();

        return "<>powerup<>%,d %s<>font<>".formatted(rewardAmt, powerupStr);
    }

    public static int getRequiredAmt(QuestType quest, int index) {
        String requirement = CONFIG.getQuestConfig().get(quest.toString()).getQuestVals()[index/2].getRequirement();

        return switch (quest) {
            case QuestType.DAILY,
                 QuestType.SPEND_BUCKS,
                 QuestType.COLLECT_BUCKS,
                 QuestType.CLONE_BOARS,
                 QuestType.SEND_GIFTS,
                 QuestType.OPEN_GIFTS,
                 QuestType.POW_WIN,
                 QuestType.POW_FAST -> Integer.parseInt(requirement);
            case QuestType.COLLECT_RARITY, QuestType.CLONE_RARITY -> 1;
        };
    }
}
