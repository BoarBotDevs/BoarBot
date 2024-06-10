package dev.boarbot.util.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
public class DataUtil {
    private static final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();

    private static final HikariConfig hikariConfig = new HikariConfig();
    private static final HikariDataSource ds;

    static {
        hikariConfig.setJdbcUrl("jdbc:mariadb://localhost:3306/boarbot");
        hikariConfig.setMaximumPoolSize(50);
        hikariConfig.setUsername(DataUtil.env.get("DB_USER"));
        hikariConfig.setPassword(DataUtil.env.get("DB_PASS"));
        ds = new HikariDataSource(hikariConfig);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
