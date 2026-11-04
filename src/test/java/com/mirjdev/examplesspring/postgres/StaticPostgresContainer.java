package com.mirjdev.examplesspring.postgres;

import org.testcontainers.postgresql.PostgreSQLContainer;

public class StaticPostgresContainer {
    public static PostgreSQLContainer getContainer() {
        return LazyPostgresContainer.INSTANCE;
    }

    private static class LazyPostgresContainer {
        private static final PostgreSQLContainer INSTANCE = makeContainer();

        private static PostgreSQLContainer makeContainer() {
            final var container = PostgresTestSupport.createContainer();
            container.start();
            return container;
        }
    }
}
