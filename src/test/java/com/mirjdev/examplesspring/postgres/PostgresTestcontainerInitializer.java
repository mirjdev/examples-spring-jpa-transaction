package com.mirjdev.examplesspring.postgres;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresTestcontainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        final var testcontainersEnabled = context.getEnvironment()
                .getProperty("omay.testcontainers.enabled");
        if (!"true".equals(testcontainersEnabled)) {
            var env = context.getEnvironment();
            env.getPropertySources().addFirst(new MapPropertySource(
                    "testcontainers",
                    // All of this is needed to completely disable
                    // anything, what could trigger errors, when running
                    // unit tests, working with databases
                    //
                    // Those tests should just be ignored if
                    // omay.testcontainers.enabled is false
                    // Use @EnabledIf(expression = "${omay.testcontainers.enabled}
                    //                loadContext = true)
                    // to disable tests working with testcontainers
                    Map.of(
                            // Running postgresql specific liquibase on
                            // h2 just triggers errors before Spring
                            // understands we should not run tests at all
//                            "spring.liquibase.enabled", "false",
                            // if hibernate tries to validate ddl it discovers
                            // that there's no tables at all and triggres
                            // errors before Spring understands we should not
                            // run tests at all
//                            "spring.jpa.hibernate.ddl-auto", "none",
                            // define omay.testcontainers.enabled if it is not
                            // defined so that
                            // @EnabledIf(expression = "${omay.testcontainers.enabled}
                            //                loadContext = true)
                            // would work
                            "omay.testcontainers.enabled", "false"
                    )
            ));
            return;
        }
        final PostgreSQLContainer<?> container = StaticPostgresContainer.getContainer();
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
