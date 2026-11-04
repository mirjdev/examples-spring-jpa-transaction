package com.mirjdev.examplesspring;

import com.mirjdev.examplesspring.config.JpaTestConfiguration;
import com.mirjdev.examplesspring.postgres.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SuppressWarnings("EmptyMethod")
@Testcontainers
@DataJpaTest
@Import(JpaTestConfiguration.class)
@ActiveProfiles("test-postgres")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresTestcontainerInitializer.class)
@Rollback(value = false)
public class ApplicationWithPostgreSQLTest {

    @Test
    void contextLoads() {
    }
}
