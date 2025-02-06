package ru.itis.pokerproject.clientserver.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static final HikariDataSource dataSource;

    static {
        try (InputStream inputStream = Database.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Файл database.properties не найден");
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(properties.getProperty("database.url"));
            hikariConfig.setUsername(properties.getProperty("database.username"));
            hikariConfig.setPassword(properties.getProperty("database.password"));
            hikariConfig.setMaximumPoolSize(Integer.parseInt(properties.getProperty("database.pool.max-size")));
            hikariConfig.setMinimumIdle(Integer.parseInt(properties.getProperty("database.pool.min-idle")));
            hikariConfig.setIdleTimeout(Integer.parseInt(properties.getProperty("database.pool.idle-timeout")));
            hikariConfig.setMaxLifetime(Integer.parseInt(properties.getProperty("database.pool.max-lifetime")));
            hikariConfig.setConnectionTimeout(
                    Integer.parseInt(properties.getProperty("database.pool.connection-timeout"))
            );
            dataSource = new HikariDataSource(hikariConfig);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации базы данных", e);
        }
    }

    private Database() {
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

    public static void closeDatabase() {
        dataSource.close();
    }
}
