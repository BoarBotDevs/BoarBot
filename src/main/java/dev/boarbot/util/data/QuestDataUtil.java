package dev.boarbot.util.data;

import dev.boarbot.util.time.TimeUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestDataUtil {
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
            WHERE quest_start_timestamp = ?;
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
}
