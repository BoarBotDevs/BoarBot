package dev.boarbot.util.data;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.time.TimeUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestDataUtil implements Configured {
    public static List<String> getQuests(Connection connection) throws SQLException {
        List<String> quests = new ArrayList<>();
        Timestamp curQuestTimestamp = new Timestamp(TimeUtil.getLastQuestResetMilli());

        String questQuery = """
            SELECT
                quest_one_id,
                quest_two_id,
                quest_three_id,
                quest_four_id,
                quest_five_id,
                quest_six_id,
                quest_seven_id
            FROM quests
            ORDER BY quest_start_timestamp DESC
            LIMIT 1;
        """;

        try (PreparedStatement statement = connection.prepareStatement(questQuery)) {
            statement.setTimestamp(1, curQuestTimestamp);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    quests.add(results.getString("quest_one_id"));
                    quests.add(results.getString("quest_two_id"));
                    quests.add(results.getString("quest_three_id"));
                    quests.add(results.getString("quest_four_id"));
                    quests.add(results.getString("quest_five_id"));
                    quests.add(results.getString("quest_six_id"));
                    quests.add(results.getString("quest_seven_id"));
                }
            }
        }

        return quests;
    }

    public static void updateQuests(Connection connection) throws SQLException {
        List<String> quests = new ArrayList<>(CONFIG.getQuestConfig().keySet());
        List<String> newQuests = new ArrayList<>();

        for (String questID : CONFIG.getQuestConfig().keySet()) {
            if (CONFIG.getQuestConfig().get(questID).isDisabled()) {
                quests.remove(questID);
            }
        }

        for (int i=0; i<7; i++) {
            int randQuest = (int) (Math.random() * quests.size());
            newQuests.add(quests.remove(randQuest));
        }

        String questQuery = """
            INSERT INTO quests (
                quest_start_timestamp,
                quest_one_id,
                quest_two_id,
                quest_three_id,
                quest_four_id,
                quest_five_id,
                quest_six_id,
                quest_seven_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;

        String userQuery = """
            UPDATE user_quests
            SET
                one_progress = 0,
                one_claimed = false,
                two_progress = 0,
                two_claimed = false,
                three_progress = 0,
                three_claimed = false,
                four_progress = 0,
                four_claimed = false,
                five_progress = 0,
                five_claimed = false,
                six_progress = 0,
                six_claimed = false,
                seven_progress = 0,
                seven_claimed = false
        """;

        try (
            PreparedStatement statement1 = connection.prepareStatement(questQuery);
            PreparedStatement statement2 = connection.prepareStatement(userQuery)
        ) {
            statement1.setTimestamp(1, new Timestamp(TimeUtil.getQuestResetMilli()));
            statement1.setString(2, newQuests.getFirst());
            statement1.setString(3, newQuests.get(1));
            statement1.setString(4, newQuests.get(2));
            statement1.setString(5, newQuests.get(3));
            statement1.setString(6, newQuests.get(4));
            statement1.setString(7, newQuests.get(5));
            statement1.setString(8, newQuests.get(6));
            statement1.execute();

            statement2.executeUpdate();
        }
    }

    public static boolean needNewQuests(Connection connection) throws SQLException {
        Timestamp questTimestamp = null;

        String questQuery = """
            SELECT quest_start_timestamp
            FROM quests
            ORDER BY quest_start_timestamp DESC
            LIMIT 1;
        """;

        try (PreparedStatement statement = connection.prepareStatement(questQuery)) {
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    questTimestamp = results.getTimestamp("quest_start_timestamp");
                }
            }
        }

        return questTimestamp == null || questTimestamp.getTime() < TimeUtil.getLastQuestResetMilli();
    }
}
