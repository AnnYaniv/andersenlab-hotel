package com.andersenlab.hotel.repository.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class JdbcConnector {

    private final HikariDataSource dataSource;

    public JdbcConnector(final String url, final String user, final String password) {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("URL", url);
        config.addDataSourceProperty("user", user);
        config.addDataSourceProperty("password", password);
        this.dataSource = new HikariDataSource(config);
    }

    @SneakyThrows
    public DataSource getDatasourse() {
        return dataSource;
    }

    public JdbcConnector migrate() {
        Flyway flyway = Flyway.configure().dataSource(dataSource).baselineOnMigrate(true)
                .locations("db/migration").load();

        flyway.migrate();
        return this;
    }
}
