package com.mirjdev.examplesspring;

import com.mirjdev.examplesspring.config.JpaTestConfiguration;
import com.mirjdev.examplesspring.postgres.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("EmptyMethod")
@DataJpaTest
@Import(JpaTestConfiguration.class)
@ActiveProfiles("test-tc-and-h2")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresTestcontainerInitializer.class)
@Rollback(value = false)
public class ApplicationWithPostgreSQLTest {

    @Test
    void contextLoads() {
    }
}