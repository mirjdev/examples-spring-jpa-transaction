package com.mirjdev.examplesspring.postgres;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

public class PostgresTestcontainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        final PostgreSQLContainer container = StaticPostgresContainer.getContainer();
        final String jdbcUrl = container.getJdbcUrl();
        var env = context.getEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource(
                "testcontainers",
                Map.of(
                        "spring.datasource.driver-class-name", "org.postgresql.Driver",
                        "spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect",
                        "spring.jpa.properties.hibernate.default_schema", "public",

                        "spring.datasource.url", jdbcUrl,
                        "spring.datasource.username", container.getUsername(),
                        "spring.datasource.password", container.getPassword(),

                        "spring.liquibase.url", jdbcUrl,
                        "spring.liquibase.user", container.getUsername(),
                        "spring.liquibase.password", container.getPassword()
                )
        ));
    }
}
