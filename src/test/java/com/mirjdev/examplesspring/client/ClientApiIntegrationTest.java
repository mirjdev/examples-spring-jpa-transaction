package com.mirjdev.examplesspring.client;

import com.jayway.jsonpath.JsonPath;
import com.mirjdev.clienttest.ClientTestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test-h2")
@SpringBootTest(classes = ClientTestApplication.class)
@AutoConfigureMockMvc
class ClientApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void cleanDatabase() {
        clientRepository.deleteAll();
    }

    @Test
    void create_returnsCreatedClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Ivan@Example.com", "  Ivan Petrov  ", "+79991234567")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/clients/\\d+$")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.fio").value("Ivan Petrov"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.version").value(0));

        assertThat(clientRepository.count()).isEqualTo(1);
    }

    @Test
    void create_duplicateEmail_returnsConflict() throws Exception {
        createViaApi("dup@example.com", "First Client", "+79990000001");

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("DUP@Example.com", "Second Client", "+79990000002")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Email already exists"))
                .andExpect(jsonPath("$.email").value("dup@example.com"));

        assertThat(clientRepository.count()).isEqualTo(1);
    }

    @Test
    void create_invalidPayload_returnsBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","fio":"","phone":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.fio").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void create_malformedJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Malformed request"));
    }

    @Test
    void getById_returnsClient() throws Exception {
        Client client = givenClient("get@example.com", "Get Client", "+79990000003");

        mockMvc.perform(get("/api/clients/{id}", client.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.email").value("get@example.com"))
                .andExpect(jsonPath("$.fio").value("Get Client"))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void getById_notFound_returnsProblemDetail() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Client not found"))
                .andExpect(jsonPath("$.clientId").value(999_999));
    }

    @Test
    void getById_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));

        mockMvc.perform(get("/api/clients/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void findAll_usesDefaultPaginationAndSorting() throws Exception {
        for (int i = 0; i < 25; i++) {
            givenClient("page" + i + "@example.com", "Page Client " + i, "+7999000" + String.format("%04d", i));
        }

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void findAll_paginatesAndSortsByAllowedField() throws Exception {
        givenClient("a@example.com", "Alpha", "+79990000011");
        givenClient("b@example.com", "Bravo", "+79990000012");
        givenClient("c@example.com", "Charlie", "+79990000013");
        givenClient("d@example.com", "Delta", "+79990000014");
        givenClient("e@example.com", "Epsilon", "+79990000015");

        mockMvc.perform(get("/api/clients")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "fio,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].fio").value("Alpha"))
                .andExpect(jsonPath("$.content[1].fio").value("Bravo"))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/clients")
                        .param("page", "2")
                        .param("size", "2")
                        .param("sort", "fio,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fio").value("Epsilon"))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void findAll_capsPageSize() throws Exception {
        givenClient("cap@example.com", "Cap Client", "+79990000016");

        mockMvc.perform(get("/api/clients").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void findAll_searchesByGlobalQueryAndEntityFields() throws Exception {
        givenClient("first@example.com", "Ivan Petrov", "+79991112233");
        givenClient("ivan@example.org", "Anna Sidorova", "+79994445566");
        givenClient("third@corp.io", "Petr Ivanov", "+79997778899");

        mockMvc.perform(get("/api/clients").param("query", "ivan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/clients").param("fio", "Petr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/clients").param("email", "example.org"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("ivan@example.org"));

        mockMvc.perform(get("/api/clients").param("phone", "444"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fio").value("Anna Sidorova"));

        mockMvc.perform(get("/api/clients")
                        .param("fio", "Ivan")
                        .param("email", "example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("first@example.com"));

        mockMvc.perform(get("/api/clients").param("query", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findAll_escapesLikeWildcards() throws Exception {
        givenClient("percent@example.com", "100% Client", "+79990000017");
        givenClient("underscore@example.com", "A_B Client", "+79990000018");
        givenClient("plain@example.com", "Simple Client", "+79990000019");

        mockMvc.perform(get("/api/clients").param("query", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fio").value("100% Client"));

        mockMvc.perform(get("/api/clients").param("query", "_"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fio").value("A_B Client"));
    }

    @Test
    void findAll_filtersByCreatedAtRange() throws Exception {
        givenClient("date@example.com", "Date Client", "+79990000020");

        mockMvc.perform(get("/api/clients")
                        .param("createdFrom", "2000-01-01T00:00:00Z")
                        .param("createdTo", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/clients").param("createdFrom", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/clients").param("createdTo", "2000-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findAll_invalidDateRange_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("createdFrom", "2100-01-01T00:00:00Z")
                        .param("createdTo", "2000-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("createdFrom must be before or equal to createdTo"));
    }

    @Test
    void findAll_invalidDateValue_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients").param("createdFrom", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.createdFrom").exists());
    }

    @Test
    void findAll_ignoresBlankFilters() throws Exception {
        givenClient("blank@example.com", "Blank Client", "+79990000021");

        mockMvc.perform(get("/api/clients")
                        .param("query", "")
                        .param("email", " ")
                        .param("fio", "")
                        .param("phone", "  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findAll_invalidSort_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients").param("sort", "password,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Unsupported sort property")));
    }

    @Test
    void update_updatesClientAndIncrementsVersion() throws Exception {
        long id = createViaApi("update@example.com", "Old Name", "+79990000030");

        mockMvc.perform(put("/api/clients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("UPDATED@Example.com", "  New Name  ", "+79991112233", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.fio").value("New Name"))
                .andExpect(jsonPath("$.phone").value("+79991112233"))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(get("/api/clients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void update_staleVersion_returnsConflict() throws Exception {
        long id = createViaApi("stale@example.com", "Stale Client", "+79990000031");

        mockMvc.perform(put("/api/clients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("stale@example.com", "First Update", "+79990000031", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(put("/api/clients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("stale@example.com", "Second Update", "+79990000031", 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Client version conflict"))
                .andExpect(jsonPath("$.clientId").value(id))
                .andExpect(jsonPath("$.expectedVersion").value(0))
                .andExpect(jsonPath("$.actualVersion").value(1));
    }

    @Test
    void update_duplicateEmail_returnsConflict() throws Exception {
        createViaApi("owner@example.com", "Owner", "+79990000032");
        long secondId = createViaApi("second@example.com", "Second", "+79990000033");

        mockMvc.perform(put("/api/clients/{id}", secondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("OWNER@example.com", "Second", "+79990000033", 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already exists"));
    }

    @Test
    void update_notFound_returnsNotFound() throws Exception {
        mockMvc.perform(put("/api/clients/{id}", 999_999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("missing@example.com", "Missing", "+79990000034", 0)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Client not found"));
    }

    @Test
    void update_missingVersion_returnsBadRequest() throws Exception {
        long id = createViaApi("noversion@example.com", "No Version", "+79990000035");

        mockMvc.perform(put("/api/clients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("noversion@example.com", "No Version", "+79990000035")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.version").exists());
    }

    @Test
    void delete_removesClient() throws Exception {
        long id = createViaApi("delete@example.com", "Delete Client", "+79990000036");

        mockMvc.perform(delete("/api/clients/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/clients/{id}", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/clients/{id}", id))
                .andExpect(status().isNotFound());
    }

    private Client givenClient(String email, String fio, String phone) {
        return clientRepository.saveAndFlush(Client.builder()
                .email(email)
                .fio(fio)
                .phone(phone)
                .build());
    }

    private long createViaApi(String email, String fio, String phone) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson(email, fio, phone)))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
        return id.longValue();
    }

    private static String clientJson(String email, String fio, String phone) {
        return """
                {"email":"%s","fio":"%s","phone":"%s"}
                """.formatted(email, fio, phone);
    }

    private static String clientJson(String email, String fio, String phone, long version) {
        return """
                {"email":"%s","fio":"%s","phone":"%s","version":%d}
                """.formatted(email, fio, phone, version);
    }
}
