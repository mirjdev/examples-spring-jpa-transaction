package com.mirjdev.examplesspring.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfiguration {

    // Spring прочитает конфиги, и применит, кроме тех, которые задаются в getHikariDataSource()
    // Мы получаем общий пулл коннектов для Liquibase(@LiquibaseDataSource) и приложения
    @Bean
    @Primary
    @LiquibaseDataSource
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties) {
        return getHikariDataSource(properties);
    }

    private HikariDataSource getHikariDataSource(DataSourceProperties properties) {
        var dataSource = new HikariDataSource();
        String driverClassName = properties.getDriverClassName();
        if (driverClassName != null) {
            dataSource.setDriverClassName(driverClassName);
        }
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        return dataSource;
    }
}
