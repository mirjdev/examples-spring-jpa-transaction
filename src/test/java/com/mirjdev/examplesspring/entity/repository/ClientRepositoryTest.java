package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.dto.ClientFilter;
import com.mirjdev.examplesspring.entity.Client;
import com.mirjdev.examplesspring.postgres.PostgresTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.default_schema=public",
        "spring.liquibase.change-log=classpath:liquibase/v_1.0/2026-11-04--01-create-table-clients.xml",
        "spring.liquibase.default-schema=public"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ClientRepositoryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-04T10:00:00Z");

    @Container
    private static final PostgreSQLContainer POSTGRES = PostgresTestSupport.createContainer();

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        PostgresTestSupport.registerProperties(registry, POSTGRES);
    }

    @Autowired
    private ClientRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void migrationProvidesIdentityTimestampAndVersionDefaults() {
        jdbcTemplate.update("insert into clients (email, fio, phone) values (?, ?, ?)",
                "defaults@example.com", "Database defaults", "+79990000001");

        Client actual = repository.findAll().getFirst();

        assertThat(actual.getId()).isPositive();
        assertThat(actual.getCreatedAt()).isNotNull();
        assertThat(actual.getVersion()).isZero();
    }

    @Test
    void migrationCreatesExpectedPostgresSchema() {
        assertThat(jdbcTemplate.queryForObject("select current_setting('server_version_num')::integer", Integer.class))
                .isBetween(180000, 189999);
        assertThat(jdbcTemplate.queryForObject("""
                select is_identity from information_schema.columns
                where table_schema = 'public' and table_name = 'clients' and column_name = 'id'
                """, String.class)).isEqualTo("YES");
        assertThat(jdbcTemplate.queryForObject("""
                select data_type from information_schema.columns
                where table_schema = 'public' and table_name = 'clients' and column_name = 'created_at'
                """, String.class)).isEqualTo("timestamp with time zone");
        assertThat(jdbcTemplate.queryForObject("""
                select character_maximum_length from information_schema.columns
                where table_schema = 'public' and table_name = 'clients' and column_name = 'email'
                """, Integer.class)).isEqualTo(254);
        assertThat(jdbcTemplate.queryForObject("""
                select constraint_type from information_schema.table_constraints
                where table_schema = 'public' and table_name = 'clients' and constraint_name = 'uk_clients_email'
                """, String.class)).isEqualTo("UNIQUE");
        assertThat(jdbcTemplate.queryForObject("""
                select indexdef from pg_indexes
                where schemaname = 'public' and tablename = 'clients' and indexname = 'idx_clients_created_at_id'
                """, String.class)).contains("(created_at, id)");
    }

    @Test
    void persistencePopulatesCreatedAtAndInitialVersion() {
        Instant before = Instant.now();
        Client client = client("created@example.com", "Created client", "+79990000002", null);

        repository.saveAndFlush(client);
        entityManager.clear();
        Client actual = repository.findById(client.getId()).orElseThrow();

        assertThat(actual.getCreatedAt()).isBetween(before.minusSeconds(1), Instant.now().plusSeconds(1));
        assertThat(actual.getVersion()).isZero();
    }

    @Test
    void createdAtCannotBeChangedByAnEntityUpdate() {
        Client client = persist("immutable@example.com", "Initial name", "+79990000003", CREATED_AT);
        client.setCreatedAt(CREATED_AT.plusSeconds(3600));
        client.setFio("Updated name");

        repository.saveAndFlush(client);
        entityManager.clear();
        Client actual = repository.findById(client.getId()).orElseThrow();

        assertThat(actual.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(actual.getFio()).isEqualTo("Updated name");
        assertThat(actual.getVersion()).isEqualTo(1L);
    }

    @Test
    void matchesAllFiltersTogetherIgnoringCaseAndIncludingDateBoundaries() {
        Client expected = persist("ivan.petrov@example.com", "Иван Петров", "+79991234567", CREATED_AT);
        persist("ivan.petrov.other@example.com", "Иван Петров", "+79991234568", CREATED_AT);
        ClientFilter filter = new ClientFilter(expected.getId(), "  PETROV@EXAMPLE  ", "  ИВАН  ", "1234567",
                CREATED_AT, CREATED_AT, 0L);

        List<Client> actual = repository.findAll(ClientSpecifications.matching(filter));

        assertThat(actual).extracting(Client::getId).containsExactly(expected.getId());
    }

    @Test
    void combinesIndependentFiltersWithAnd() {
        persist("first@example.com", "First client", "+79990000004", CREATED_AT);
        persist("second@example.com", "Second client", "+79990000005", CREATED_AT);

        List<Client> actual = repository.findAll(ClientSpecifications.matching(
                new ClientFilter(null, "first@", "Second", null, null, null, null)));

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "fio", "phone"})
    void appliesEachTextFilterIndependently(String field) {
        Client expected = persist("target@example.com", "Иван Сидоров", "+79991234567", CREATED_AT);
        persist("other@example.com", "Петр Иванов", "+78880000000", CREATED_AT);
        ClientFilter filter = switch (field) {
            case "email" -> new ClientFilter(null, "TARGET@", null, null, null, null, null);
            case "fio" -> new ClientFilter(null, null, "СИДОРОВ", null, null, null, null);
            case "phone" -> new ClientFilter(null, null, null, "1234567", null, null, null);
            default -> throw new IllegalArgumentException("Unexpected filter: " + field);
        };

        assertThat(repository.findAll(ClientSpecifications.matching(filter)))
                .extracting(Client::getId).containsExactly(expected.getId());
    }

    @Test
    void filtersByIdAndVersionExactly() {
        Client client = persist("version@example.com", "Versioned client", "+79990000006", CREATED_AT);
        client.setFio("Updated versioned client");
        repository.saveAndFlush(client);
        ClientFilter matching = new ClientFilter(client.getId(), null, null, null, null, null, 1L);
        ClientFilter oldVersion = new ClientFilter(client.getId(), null, null, null, null, null, 0L);
        ClientFilter otherId = new ClientFilter(client.getId() + 1, null, null, null, null, null, null);

        assertThat(repository.findAll(ClientSpecifications.matching(matching)))
                .extracting(Client::getId).containsExactly(client.getId());
        assertThat(repository.findAll(ClientSpecifications.matching(oldVersion))).isEmpty();
        assertThat(repository.findAll(ClientSpecifications.matching(otherId))).isEmpty();
    }

    @Test
    void appliesEachDateBoundaryAndExcludesDatesOutsideTheRange() {
        Client early = persist("early@example.com", "Early", "+79990000007", CREATED_AT.minusSeconds(1));
        Client middle = persist("middle@example.com", "Middle", "+79990000008", CREATED_AT);
        Client late = persist("late@example.com", "Late", "+79990000009", CREATED_AT.plusSeconds(1));

        assertThat(repository.findAll(ClientSpecifications.matching(
                new ClientFilter(null, null, null, null, CREATED_AT, null, null))))
                .extracting(Client::getId).containsExactlyInAnyOrder(middle.getId(), late.getId());
        assertThat(repository.findAll(ClientSpecifications.matching(
                new ClientFilter(null, null, null, null, null, CREATED_AT, null))))
                .extracting(Client::getId).containsExactlyInAnyOrder(early.getId(), middle.getId());
        assertThat(repository.findAll(ClientSpecifications.matching(
                new ClientFilter(null, null, null, null, CREATED_AT, CREATED_AT, null))))
                .extracting(Client::getId).containsExactly(middle.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_", "\\"})
    void treatsLikeWildcardsAndEscapeCharacterAsLiteralText(String literal) {
        Client expected = persist("literal@example.com", "Contains " + literal + " character", "+79990000010", CREATED_AT);
        persist("plain@example.com", "Contains plain character", "+79990000011", CREATED_AT);

        List<Client> actual = repository.findAll(ClientSpecifications.matching(
                new ClientFilter(null, null, literal, null, null, null, null)));

        assertThat(actual).extracting(Client::getId).containsExactly(expected.getId());
    }

    @Test
    void ignoresBlankAndMissingTextFilters() {
        Client expected = persist("blank@example.com", "Blank filters", "+79990000012", CREATED_AT);

        List<Client> actual = repository.findAll(ClientSpecifications.matching(
                new ClientFilter(null, "  ", "", "\t", null, null, null)));

        assertThat(actual).extracting(Client::getId).containsExactly(expected.getId());
    }

    @Test
    void returnsStablePagesAndTotalCountWhenSortValuesAreEqual() {
        Client first = persist("page1@example.com", "Same name", "+79990000013", CREATED_AT);
        Client second = persist("page2@example.com", "Same name", "+79990000014", CREATED_AT);
        Client third = persist("page3@example.com", "Same name", "+79990000015", CREATED_AT);
        ClientFilter filter = new ClientFilter(null, "page", null, null, null, null, null);
        Sort sort = Sort.by("fio", "id");

        Page<Client> firstPage = repository.findAll(ClientSpecifications.matching(filter), PageRequest.of(0, 2, sort));
        Page<Client> secondPage = repository.findAll(ClientSpecifications.matching(filter), PageRequest.of(1, 2, sort));

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(Stream.concat(firstPage.stream(), secondPage.stream()).map(Client::getId).toList())
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    @Test
    void migrationEnforcesUniqueEmail() {
        persist("unique@example.com", "First client", "+79990000016", CREATED_AT);

        assertThatThrownBy(() -> jdbcTemplate.update("insert into clients (email, fio, phone) values (?, ?, ?)",
                "unique@example.com", "Duplicate client", "+79990000017"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "fio", "phone"})
    void migrationEnforcesRequiredFields(String missingField) {
        String email = missingField.equals("email") ? null : "required@example.com";
        String fio = missingField.equals("fio") ? null : "Required fields";
        String phone = missingField.equals("phone") ? null : "+79990000018";

        assertThatThrownBy(() -> jdbcTemplate.update("insert into clients (email, fio, phone) values (?, ?, ?)",
                email, fio, phone)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rejectsAConcurrentUpdateWithoutOverwritingTheCommittedChanges() {
        Client client = persist("concurrent@example.com", "Original name", "+79990000019", CREATED_AT);
        try (EntityManager firstManager = entityManagerFactory.createEntityManager();
             EntityManager secondManager = entityManagerFactory.createEntityManager()) {
            firstManager.getTransaction().begin();
            secondManager.getTransaction().begin();
            try {
                Client firstCopy = firstManager.find(Client.class, client.getId());
                Client secondCopy = secondManager.find(Client.class, client.getId());
                firstCopy.setFio("Committed name");
                firstManager.getTransaction().commit();
                secondCopy.setFio("Stale name");

                assertThatExceptionOfType(OptimisticLockException.class).isThrownBy(secondManager::flush);
            } finally {
                if (firstManager.getTransaction().isActive()) {
                    firstManager.getTransaction().rollback();
                }
                if (secondManager.getTransaction().isActive()) {
                    secondManager.getTransaction().rollback();
                }
            }
            Client actual = repository.findById(client.getId()).orElseThrow();
            assertThat(actual.getFio()).isEqualTo("Committed name");
            assertThat(actual.getVersion()).isEqualTo(1L);
        } finally {
            repository.deleteById(client.getId());
        }
    }

    private Client persist(String email, String fio, String phone, Instant createdAt) {
        return repository.saveAndFlush(client(email, fio, phone, createdAt));
    }

    private static Client client(String email, String fio, String phone, Instant createdAt) {
        Client client = new Client();
        client.setEmail(email);
        client.setFio(fio);
        client.setPhone(phone);
        client.setCreatedAt(createdAt);
        return client;
    }
}
