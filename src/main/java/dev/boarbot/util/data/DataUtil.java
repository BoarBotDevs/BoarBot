package dev.boarbot.util.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.EnvironmentType;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataUtil {
    private static final HikariConfig hikariConfig = new HikariConfig();
    private static HikariDataSource ds;

    public static void setupDatabase() {
        if (BoarBotApp.getEnvironmentType() == EnvironmentType.PROD) {
            hikariConfig.setJdbcUrl("jdbc:mariadb://db:3306/boarbot");
        } else {
            hikariConfig.setJdbcUrl("jdbc:mariadb://localhost:3306/boarbot");
        }

        hikariConfig.setMaximumPoolSize(50);
        hikariConfig.setUsername("default");
        hikariConfig.setPassword(BoarBotApp.getEnv("DB_PASS"));
        ds = new HikariDataSource(hikariConfig);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static boolean databaseHasData() throws SQLException {
        try (Connection connection = getConnection()) {
            String query = """
                SELECT 1
                FROM users;
            """;

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
