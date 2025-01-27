package dev.boarbot.util.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.DatabaseUpdater;
import dev.boarbot.bot.EnvironmentType;
import dev.boarbot.util.logging.Log;


import java.sql.*;

public class DataUtil {
    private static final HikariConfig hikariConfig = new HikariConfig();
    private static HikariDataSource ds;

    private final static String host = BoarBotApp.getEnvironmentType() == EnvironmentType.PROD
        ? "db"
        : "localhost";

    public static void setupDatabase() {
        hikariConfig.setJdbcUrl("jdbc:mariadb://%s:3306".formatted(host));
        hikariConfig.setMaximumPoolSize(50);
        hikariConfig.setUsername("default");
        hikariConfig.setPassword(BoarBotApp.getEnv("DB_PASS"));

        try (HikariDataSource dsTemp = new HikariDataSource(hikariConfig)) {
            try (Connection connection = dsTemp.getConnection()) {
                createDatabaseIfNotExist(connection);
            }
        } catch (SQLException exception) {
            Log.error(DatabaseUpdater.class, "Something went wrong when creating database", exception);
            System.exit(-1);
        }

        hikariConfig.setJdbcUrl(hikariConfig.getJdbcUrl() + "/boarbot?allowMultiQueries=true");
        ds = new HikariDataSource(hikariConfig);

        try (Connection connection = DataUtil.getConnection()) {
            createSchemaTableIfNotExist(connection);
        } catch (SQLException exception) {
            Log.error(DatabaseUpdater.class, "Something went wrong when creating schema table", exception);
            System.exit(-1);
        }
    }

    public static void createDatabaseIfNotExist(Connection connection) {
        String sql = """
            CREATE DATABASE IF NOT EXISTS boarbot;
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException exception) {
            Log.error(DatabaseUpdater.class, "Something went wrong when creating the database", exception);
            System.exit(-1);
        }
    }

    public static void createSchemaTableIfNotExist(Connection connection) {
        String sql = """
            CREATE TABLE IF NOT EXISTS schema_version (
                id INT AUTO_INCREMENT PRIMARY KEY,
                script_name VARCHAR(255) NOT NULL,
                executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            Log.error(DatabaseUpdater.class, "Something went wrong when creating the schema table", exception);
            System.exit(-1);
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
