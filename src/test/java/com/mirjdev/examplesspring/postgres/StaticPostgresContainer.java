package com.mirjdev.examplesspring.postgres;

import org.testcontainers.containers.PostgreSQLContainer;

public class StaticPostgresContainer {
    public static PostgreSQLContainer<?> getContainer() {
        return LazyPostgresContainer.INSTANCE;
    }

    private static class LazyPostgresContainer {
        private static final PostgreSQLContainer<?> INSTANCE = makeContainer();

        private static PostgreSQLContainer<?> makeContainer() {
            final var container = new PostgreSQLContainer<>("postgres:14.6-alpine3.17")
                    .withDatabaseName("dev")
                    .withUsername("postgres")
                    .withPassword("root");
            container.start();
            return container;
        }
    }
}
