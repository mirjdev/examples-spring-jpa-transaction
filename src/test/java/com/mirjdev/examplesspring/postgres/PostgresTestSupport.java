package com.mirjdev.examplesspring.postgres;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Uses the same PostgreSQL version as postgres-docker/docker-compose.yml. */
public final class PostgresTestSupport {

    private PostgresTestSupport() {
    }

    public static PostgreSQLContainer createContainer() {
        return new PostgreSQLContainer("postgres:18.4")
                .withDatabaseName("dev")
                .withUsername("postgres")
                .withPassword("postgres");
    }

    public static void registerProperties(DynamicPropertyRegistry registry, PostgreSQLContainer container) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.liquibase.url", container::getJdbcUrl);
        registry.add("spring.liquibase.user", container::getUsername);
        registry.add("spring.liquibase.password", container::getPassword);
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "public");
        registry.add("spring.liquibase.default-schema", () -> "public");
    }
}
