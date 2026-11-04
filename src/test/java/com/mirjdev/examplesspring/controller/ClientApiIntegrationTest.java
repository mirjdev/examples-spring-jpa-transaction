package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.dto.ClientCreateRequest;
import com.mirjdev.examplesspring.dto.ClientPageResponse;
import com.mirjdev.examplesspring.dto.ClientResponse;
import com.mirjdev.examplesspring.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.entity.Client;
import com.mirjdev.examplesspring.entity.repository.ClientRepository;
import com.mirjdev.examplesspring.postgres.PostgresTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.liquibase.enabled=true",
        "spring.liquibase.contexts=test",
        "spring.liquibase.change-log=classpath:liquibase/db.changelog-master.xml"
})
@AutoConfigureMockMvc
class ClientApiIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = PostgresTestSupport.createContainer();

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        PostgresTestSupport.registerProperties(registry, POSTGRES);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void appliesClientMigrationThroughTheProductionMasterChangelog() {
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from databasechangelog where id = ?", Long.class,
                "2026-11-04--01-create-table-clients")).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from pg_indexes where schemaname = 'public' and tablename = 'clients' "
                        + "and indexname = 'idx_clients_created_at_id'", Long.class)).isEqualTo(1L);
    }

    @Test
    void persistsReadsUpdatesAndDeletesClientWithVersionAndImmutableCreationTimestamp() throws Exception {
        ClientResponse created = create("IVAN@EXAMPLE.COM", "  Иван Иванов  ", "+79991234567");

        assertThat(created.id()).isPositive();
        assertThat(created.email()).isEqualTo("ivan@example.com");
        assertThat(created.fio()).isEqualTo("Иван Иванов");
        assertThat(created.version()).isZero();
        assertThat(created.createdAt()).isNotNull();
        ClientResponse fetched = read(created.id());
        assertThat(fetched).isEqualTo(created);

        ClientResponse updated = update(created.id(), new ClientUpdateRequest(null, "  Пётр Петров  ", null, created.version()));

        assertThat(updated.fio()).isEqualTo("Пётр Петров");
        assertThat(updated.email()).isEqualTo(created.email());
        assertThat(updated.phone()).isEqualTo(created.phone());
        assertThat(updated.createdAt()).isEqualTo(created.createdAt());
        assertThat(updated.version()).isEqualTo(created.version() + 1);
        assertThat(read(created.id())).isEqualTo(updated);

        mvc.perform(delete("/api/clients/{id}", created.id())).andExpect(status().isNoContent());
        mvc.perform(get("/api/clients/{id}", created.id())).andExpect(status().isNotFound());
        assertThat(repository.findById(created.id())).isEmpty();
    }

    @Test
    void enforcesNormalizedEmailUniquenessAndRollsBackFailedUpdate() throws Exception {
        ClientResponse first = create("first@example.com", "Первый Клиент", "+79991234567");
        ClientResponse second = create("second@example.com", "Второй Клиент", "+78881234567");

        mvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClientCreateRequest("FIRST@EXAMPLE.COM", "Дубликат", "+77771234567"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("status").value(409));

        mvc.perform(put("/api/clients/{id}", second.id()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClientUpdateRequest("FIRST@EXAMPLE.COM", "Не сохранять", null, second.version()))))
                .andExpect(status().isConflict());

        assertThat(repository.count()).isEqualTo(2);
        assertThat(read(first.id())).isEqualTo(first);
        assertThat(read(second.id())).isEqualTo(second);
    }

    @Test
    void rejectsStaleUpdateWithoutOverwritingCommittedChanges() throws Exception {
        ClientResponse original = create("lock@example.com", "Исходное имя", "+79991234567");
        ClientResponse current = update(original.id(), new ClientUpdateRequest(null, "Актуальное имя", null, original.version()));

        mvc.perform(put("/api/clients/{id}", original.id()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClientUpdateRequest(null, "Устаревшее имя", null, original.version()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("status").value(409));

        assertThat(read(original.id())).isEqualTo(current);
    }

    @Test
    void combinesAllFiltersAndIncludesCreationDateBoundaries() throws Exception {
        Instant createdAt = Instant.parse("2026-10-01T10:00:00Z");
        Client matching = fixture("alpha@example.com", "Иван Иванов", "+79991234567", createdAt);
        fixture("alpha-other@example.com", "Пётр Петров", "+78881234567", createdAt.plusSeconds(1));
        fixture("beta@example.com", "Иван Иванов", "+79991234567", createdAt.minusSeconds(1));

        mvc.perform(get("/api/clients")
                        .param("id", matching.getId().toString())
                        .param("email", "ALPHA")
                        .param("fio", "ИВАН")
                        .param("phone", "1234")
                        .param("createdFrom", createdAt.toString())
                        .param("createdTo", createdAt.toString())
                        .param("version", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("content[0].id").value(matching.getId()));

        mvc.perform(get("/api/clients").param("email", "alpha").param("fio", "несуществующий"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements").value(0));
        mvc.perform(get("/api/clients").param("id", matching.getId().toString()).param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements").value(0));
    }

    @Test
    void treatsSqlWildcardCharactersAsLiteralSearchTextAndIgnoresBlankFilters() throws Exception {
        Instant createdAt = Instant.parse("2026-10-01T10:00:00Z");
        Client matching = fixture("literal@example.com", "Иван 100%_\\ тест", "+79991234567", createdAt);
        fixture("other@example.com", "Иван 100aa\\ тест", "+78881234567", createdAt);

        mvc.perform(get("/api/clients").param("fio", "%_\\"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("content[0].id").value(matching.getId()));
        mvc.perform(get("/api/clients").param("email", "  ").param("fio", "").param("phone", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalElements").value(2));
    }

    @Test
    void returnsStableNonOverlappingPagesWhenPrimarySortValuesAreEqual() throws Exception {
        Instant createdAt = Instant.parse("2026-10-01T10:00:00Z");
        Client first = fixture("page1@example.com", "Одинаковое имя", "+79991234561", createdAt);
        Client second = fixture("page2@example.com", "Одинаковое имя", "+79991234562", createdAt);
        Client third = fixture("page3@example.com", "Одинаковое имя", "+79991234563", createdAt);

        String firstPageJson = mvc.perform(get("/api/clients").param("page", "0").param("size", "2").param("sort", "fio,asc"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String secondPageJson = mvc.perform(get("/api/clients").param("page", "1").param("size", "2").param("sort", "fio,asc"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ClientPageResponse firstPage = objectMapper.readValue(firstPageJson, ClientPageResponse.class);
        ClientPageResponse secondPage = objectMapper.readValue(secondPageJson, ClientPageResponse.class);

        assertThat(firstPage.content()).extracting(ClientResponse::id).containsExactly(first.getId(), second.getId());
        assertThat(secondPage.content()).extracting(ClientResponse::id).containsExactly(third.getId());
        assertThat(firstPage.page()).isZero();
        assertThat(secondPage.page()).isEqualTo(1);
        assertThat(firstPage.size()).isEqualTo(2);
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);

        mvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").value(0))
                .andExpect(jsonPath("size").value(20));
    }

    @Test
    void rejectsInvalidRangeAndUnsupportedSortBeforeQueryingClients() throws Exception {
        mvc.perform(get("/api/clients")
                        .param("createdFrom", "2026-10-02T00:00:00Z")
                        .param("createdTo", "2026-10-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/clients").param("sort", "unknownField,asc"))
                .andExpect(status().isBadRequest());
    }

    private ClientResponse create(String email, String fio, String phone) throws Exception {
        String body = mvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClientCreateRequest(email, fio, phone))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, ClientResponse.class);
    }

    private ClientResponse read(Long id) throws Exception {
        String body = mvc.perform(get("/api/clients/{id}", id))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, ClientResponse.class);
    }

    private ClientResponse update(Long id, ClientUpdateRequest request) throws Exception {
        String body = mvc.perform(put("/api/clients/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, ClientResponse.class);
    }

    private Client fixture(String email, String fio, String phone, Instant createdAt) {
        Client client = new Client();
        client.setEmail(email);
        client.setFio(fio);
        client.setPhone(phone);
        client.setCreatedAt(createdAt);
        return repository.saveAndFlush(client);
    }

}
