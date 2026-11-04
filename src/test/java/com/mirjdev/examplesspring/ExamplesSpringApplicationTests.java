package com.mirjdev.examplesspring;

import com.mirjdev.examplesspring.postgres.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test-postgres")
@ContextConfiguration(initializers = PostgresTestcontainerInitializer.class)
@SpringBootTest
class ExamplesSpringApplicationTests {

    @Test
    void contextLoads() {
    }

}
