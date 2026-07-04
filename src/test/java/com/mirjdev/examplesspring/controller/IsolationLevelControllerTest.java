package com.mirjdev.examplesspring.controller;

import tools.jackson.databind.ObjectMapper;
import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.repository.DriverRepository;
import com.mirjdev.examplesspring.postgres.PostgresTestcontainerInitializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@ActiveProfiles("test-tc-and-h2")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresTestcontainerInitializer.class)
@SpringBootTest
@AutoConfigureMockMvc
class IsolationLevelControllerTest {

    @Autowired
    private IsolationLevelController isolationLevelController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
        driverRepository.saveAndFlush(
                Driver.builder()
                        .id(1L)
                        .fio("fio_ver1")
                        .driverLicense("A1")
                        .build()
        );
    }

    @Test
    void testRR() throws Exception {
        String content = mockMvc.perform(get("/api/isolation-level/test-rr"))
                .andDo(print())
                .andExpect(status().isIAmATeapot()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertThat(content).contains("ERROR: could not serialize access due to concurrent update");
    }
}