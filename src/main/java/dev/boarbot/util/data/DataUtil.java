package dev.boarbot.util.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.boarbot.BoarBotApp;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DataUtil {
    private static final HikariConfig hikariConfig = new HikariConfig();
    private static final HikariDataSource ds;

    static {
        hikariConfig.setJdbcUrl("jdbc:mariadb://localhost:3306/boarbot");
        hikariConfig.setMaximumPoolSize(50);
        hikariConfig.setUsername(BoarBotApp.getEnv().get("DB_USER"));
        hikariConfig.setPassword(BoarBotApp.getEnv().get("DB_PASS"));
        ds = new HikariDataSource(hikariConfig);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
