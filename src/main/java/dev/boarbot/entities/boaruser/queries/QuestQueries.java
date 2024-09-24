package dev.boarbot.entities.boaruser.queries;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.quests.IndivQuestConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.data.QuestData;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.data.QuestDataUtil;
import dev.boarbot.util.quests.QuestInfo;
import dev.boarbot.util.quests.QuestUtil;
import dev.boarbot.util.time.TimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestQueries implements Configured {
    private final BoarUser boarUser;

    private final static Map<Integer, String> numStrs = new HashMap<>();

    static {
        numStrs.put(0, "one");
        numStrs.put(1, "two");
        numStrs.put(2, "three");
        numStrs.put(3, "four");
        numStrs.put(4, "five");
        numStrs.put(5, "six");
        numStrs.put(6, "seven");
    }

    public QuestQueries(BoarUser boarUser) {
        this.boarUser = boarUser;
    }

    public QuestInfo claimQuests(QuestData questData, Connection connection) throws SQLException {
        List<QuestType> quests = QuestDataUtil.getQuests(connection);
        List<Integer> questIndexes = new ArrayList<>();
        List<IndivQuestConfig> claimQuests = new ArrayList<>();

        for (int i=0; i<quests.size(); i++) {
            QuestType quest = quests.get(i);
            int progress = questData.questProgress().get(i);
            boolean claimed = questData.questClaims().get(i);

            if (claimed) {
                continue;
            }

            int requiredAmt = QuestUtil.getRequiredAmt(quest, i, false);
            boolean shouldClaim = progress >= requiredAmt;

            if (shouldClaim) {
                questIndexes.add(i);
                claimQuests.add(CONFIG.getQuestConfig().get(quest.toString()).getQuestVals()[i/2]);
            }
        }

        if (claimQuests.isEmpty()) {
            return null;
        }

        String updateQuery = """
            UPDATE user_quests
            SET
                one_claimed = one_claimed + ?,
                two_claimed = two_claimed + ?,
                three_claimed = three_claimed + ?,
                four_claimed = four_claimed + ?,
                five_claimed = five_claimed + ?,
                six_claimed = six_claimed + ?,
                seven_claimed = seven_claimed + ?
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            for (int i=0; i<quests.size(); i++) {
                if (questIndexes.contains(i)) {
                    statement.setInt(i+1, 1);
                    continue;
                }

                statement.setInt(i+1, 0);
            }

            statement.setString(8, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        for (int i=0; i<claimQuests.size(); i++) {
            IndivQuestConfig claimQuest = claimQuests.get(i);
            this.giveReward(claimQuest, connection);
            Log.debug(this.boarUser.getUser(), this.getClass(), "Claimed quest %s".formatted(quests.get(i)));
        }

        return new QuestInfo(claimQuests, false);
    }

    public boolean claimBonus(QuestData questData, Connection connection) throws SQLException {
        if (questData.fullClaimed()) {
            return false;
        }

        String updateQuery = """
            UPDATE user_quests
            SET full_claimed = full_claimed + 1
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        this.giveBonus(connection);
        Log.debug(this.boarUser.getUser(), this.getClass(), "Claimed quest bonus");

        return true;
    }

    public void toggleAutoClaim(Connection connection) throws SQLException {
        String updateQuery = """
            UPDATE user_quests
            SET auto_claim = !auto_claim
            WHERE user_id = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, this.boarUser.getUserID());
            statement.executeUpdate();
        }

        Log.debug(this.boarUser.getUser(), this.getClass(), "Toggled auto claim");
    }

    public QuestInfo addProgress(
        QuestType quest, List<String> boarIDs, Connection connection
    ) throws SQLException {
        int questIndex = QuestDataUtil.getQuestIndex(quest, connection);

        if (questIndex == -1) {
            return null;
        }

        String requiredRarity = this.getRequiredRarity(quest, questIndex);
        int val = 0;

        for (String boarID : boarIDs) {
            if (BoarUtil.findRarityKey(boarID).equals(requiredRarity)) {
                val++;
            }
        }

        return this.addProgress(quest, questIndex, val, connection);
    }

    public QuestInfo addProgress(QuestType quest, long val, Connection connection) throws SQLException {
        int questIndex = QuestDataUtil.getQuestIndex(quest, connection);

        if (questIndex == -1) {
            return null;
        }

        return this.addProgress(quest, questIndex, val, connection);
    }

    private QuestInfo addProgress(
        QuestType quest, int questIndex, long val, Connection connection
    ) throws SQLException {
        String columnNum = numStrs.get(questIndex);
        List<QuestType> quests = QuestDataUtil.getQuests(connection);
        List<Integer> requiredAmts = new ArrayList<>();
        List<Integer> actualRequiredAmts = new ArrayList<>();

        for (int i=0; i<quests.size(); i++) {
            QuestType curQuest = quests.get(i);

            requiredAmts.add(QuestUtil.getRequiredAmt(curQuest, i, false));
            actualRequiredAmts.add(QuestUtil.getRequiredAmt(curQuest, i, true));
        }

        if (quest.equals(QuestType.POW_FAST)) {
            val = val < actualRequiredAmts.get(questIndex) ? 1 : 0;
        }

        if (val == 0) {
            return null;
        }

        String query = """
            SELECT
                one_progress,
                two_progress,
                three_progress,
                four_progress,
                five_progress,
                six_progress,
                seven_progress,
                %s_claimed,
                full_claimed,
                auto_claim
            FROM user_quests
            WHERE user_id = ?;
        """.formatted(columnNum);

        boolean shouldClaim = false;
        boolean shouldClaimBonus = true;
        int progress = 0;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.boarUser.getUserID());

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    progress = results.getInt("%s_progress".formatted(columnNum));
                    boolean complete = progress + val >= requiredAmts.get(questIndex);
                    boolean claimed = results.getBoolean("%s_claimed".formatted(columnNum));
                    boolean autoClaim = results.getBoolean("auto_claim");
                    shouldClaim = complete && autoClaim && !claimed;
                    shouldClaimBonus = autoClaim && !results.getBoolean("full_claimed");

                    for (int i=0; i<quests.size() && shouldClaimBonus; i++) {
                        int curProgress = results.getInt("%s_progress".formatted(numStrs.get(i)));

                        if (i == questIndex) {
                            shouldClaimBonus = complete;
                        } else {
                            shouldClaimBonus = curProgress >= requiredAmts.get(i);
                        }
                    }
                }
            }
        }

        String update = """
            UPDATE user_quests
            SET
                %s_progress = ?,
                %s_claimed = (%s_claimed OR ?),
                full_claimed = (full_claimed OR ?),
                fastest_full_millis = LEAST(fastest_full_millis, ?)
            WHERE user_id = ?;
        """.formatted(columnNum, columnNum, columnNum);

        try (PreparedStatement statement = connection.prepareStatement(update)) {
            statement.setLong(1, progress + val);
            statement.setBoolean(2, shouldClaim);
            statement.setBoolean(3, shouldClaimBonus);
            statement.setLong(4, shouldClaimBonus
                ? TimeUtil.getCurMilli() - TimeUtil.getLastQuestResetMilli()
                : Integer.MAX_VALUE
            );
            statement.setString(5, this.boarUser.getUserID());
            statement.executeUpdate();
        }
        List<IndivQuestConfig> questConfigs = new ArrayList<>();
        IndivQuestConfig questConfig = CONFIG.getQuestConfig().get(quest.toString()).getQuestVals()[questIndex/2];
        String rewardType = questConfig.getRewardType();
        int rewardAmt = questConfig.getRewardAmt();

        Log.debug(
            this.boarUser.getUser(),
            this.getClass(),
            "Added %,d progress for %s quest".formatted(val, quest)
        );

        if (rewardType.equals("bucks")) {
            QuestInfo bucksQuest = this.addProgress(QuestType.COLLECT_BUCKS, rewardAmt, connection);

            if (bucksQuest != null) {
                questConfigs.add(bucksQuest.quests().getFirst());
                shouldClaimBonus = shouldClaimBonus || bucksQuest.gaveBonus();
            }
        }

        if (shouldClaimBonus) {
            this.giveBonus(connection);
            Log.debug(this.boarUser.getUser(), this.getClass(), "Auto-claimed quest bonus");
        }

        if (shouldClaim) {
            questConfigs.add(questConfig);
            this.giveReward(questConfig, connection);
            Log.debug(this.boarUser.getUser(), this.getClass(), "Auto-claimed quest %s".formatted(quest));
            return new QuestInfo(questConfigs, shouldClaimBonus);
        }

        return null;
    }

    private void giveReward(IndivQuestConfig questConfig, Connection connection) throws SQLException {
        String rewardType = questConfig.getRewardType();
        int rewardAmt = questConfig.getRewardAmt();

        if (rewardType.equals("bucks")) {
            this.boarUser.baseQuery().giveBucks(connection, rewardAmt);
        } else {
            this.boarUser.powQuery().addPowerup(connection, rewardType, rewardAmt, true);
        }
    }

    private void giveBonus(Connection connection) throws SQLException {
        this.boarUser.powQuery().addPowerup(connection, "transmute", NUMS.getQuestBonusAmt(), true);
    }

    private String getRequiredRarity(QuestType quest, int index) {
        if (quest.equals(QuestType.COLLECT_RARITY) || quest.equals(QuestType.CLONE_RARITY)) {
            return CONFIG.getQuestConfig().get(quest.toString()).getQuestVals()[index/2].getRequirement();
        }

        throw new IllegalArgumentException("Unsupported quest type: " + quest);
    }
}
