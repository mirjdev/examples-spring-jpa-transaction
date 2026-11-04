package com.mirjdev.examplesspring.client;

import com.mirjdev.clienttest.ClientTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test-h2")
@SpringBootTest(classes = ClientTestApplication.class)
class ClientContextSmokeTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void contextLoadsAndLiquibaseMigrationIsApplied() {
        assertThat(clientRepository.count()).isZero();
    }
}
