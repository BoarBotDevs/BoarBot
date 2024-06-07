package dev.boarbot.util.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GuildDataUtil {
    public static ArrayList<String> getValidChannelIDs(Connection connection, String guildID) throws SQLException {
        ArrayList<String> channelIDs = new ArrayList<>();

        String query = "SELECT channel_one, channel_two, channel_three FROM guilds WHERE guild_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, guildID);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    if (results.getString("channel_one") != null) {
                        channelIDs.add(results.getString("channel_one"));
                    }

                    if (results.getString("channel_two") != null) {
                        channelIDs.add(results.getString("channel_two"));
                    }

                    if (results.getString("channel_three") != null) {
                        channelIDs.add(results.getString("channel_three"));
                    }
                }
            }
        }

        return channelIDs;
    }
}
