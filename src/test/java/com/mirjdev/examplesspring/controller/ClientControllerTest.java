package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.config.ClientWebConfiguration;
import com.mirjdev.examplesspring.dto.ClientFilter;
import com.mirjdev.examplesspring.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.entity.Client;
import com.mirjdev.examplesspring.exception.ClientConflictException;
import com.mirjdev.examplesspring.exception.ClientNotFoundException;
import com.mirjdev.examplesspring.mapper.ClientMapperImpl;
import com.mirjdev.examplesspring.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@Import({ClientMapperImpl.class, ClientExceptionHandler.class, ClientWebConfiguration.class})
class ClientControllerTest {

    private static final String URL = "/api/clients";
    private static final Instant CREATED_AT = Instant.parse("2026-11-04T12:00:00Z");
    private static final String CREATE_BODY = """
            {"email":"ivan@example.com","fio":"Иван Иванов","phone":"+79991234567"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService service;

    @Test
    void createsClientAndReturnsLocationAndServerManagedFields() throws Exception {
        when(service.create(any(Client.class))).thenReturn(client());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/clients/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.fio").value("Иван Иванов"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.version").value(0));

        var captor = ArgumentCaptor.forClass(Client.class);
        verify(service).create(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getCreatedAt()).isNull();
        assertThat(captor.getValue().getVersion()).isNull();
        assertThat(captor.getValue().getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void returnsClientById() throws Exception {
        when(service.findById(1L)).thenReturn(client());

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void updatesOnlyProvidedFieldsUsingExpectedVersion() throws Exception {
        Client updated = client();
        updated.setFio("Петр Петров");
        updated.setVersion(1L);
        when(service.update(eq(1L), any(ClientUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fio":"Петр Петров","version":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("Петр Петров"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.version").value(1));

        verify(service).update(1L, new ClientUpdateRequest(null, "Петр Петров", null, 0L));
    }

    @Test
    void deletesClientWithoutResponseBody() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).delete(1L);
    }

    @Test
    void listsClientsWithDefaultPageAndStableSort() throws Exception {
        when(service.findAll(any(ClientFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(client()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(eq(new ClientFilter(null, null, null, null, null, null, null)), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
        assertThat(pageable.getValue().getSort().getOrderFor("id")).isEqualTo(Sort.Order.asc("id"));
    }

    @Test
    void bindsEverySearchFieldAndRequestedPaginationAndSort() throws Exception {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-12-31T23:59:59Z");
        when(service.findAll(any(ClientFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(client()), PageRequest.of(2, 3), 11));

        mockMvc.perform(get(URL)
                        .param("id", "1")
                        .param("email", "example")
                        .param("fio", "Иван")
                        .param("phone", "7999")
                        .param("createdFrom", from.toString())
                        .param("createdTo", to.toString())
                        .param("version", "0")
                        .param("page", "2")
                        .param("size", "3")
                        .param("sort", "fio,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(4));

        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(eq(new ClientFilter(1L, "example", "Иван", "7999", from, to, 0L)), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(3);
        assertThat(pageable.getValue().getSort().getOrderFor("fio")).isEqualTo(Sort.Order.desc("fio"));
    }

    @Test
    void returnsEmptyPage() throws Exception {
        when(service.findAll(any(ClientFilter.class), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 20)));

        mockMvc.perform(get(URL).param("email", "absent@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void returnsProblemDetailWhenClientDoesNotExist() throws Exception {
        when(service.findById(99L)).thenThrow(new ClientNotFoundException(99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void returnsNotFoundForUpdateOfMissingClient() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new ClientNotFoundException(99L));

        mockMvc.perform(put(URL + "/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fio\":\"Иван Иванов\",\"version\":0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForDeletionOfMissingClient() throws Exception {
        doThrow(new ClientNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete(URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictForStaleVersion() throws Exception {
        when(service.update(eq(1L), any())).thenThrow(new ClientConflictException("Версия клиента устарела"));

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fio\":\"Иван Иванов\",\"version\":0}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void returnsConflictForConcurrentDatabaseUpdate() throws Exception {
        when(service.update(eq(1L), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Client.class, 1L));

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fio\":\"Иван Иванов\",\"version\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void doesNotExposeDatabaseDetailsForDuplicateEmail() throws Exception {
        when(service.create(any())).thenThrow(new DataIntegrityViolationException("private SQL details"));

        String response = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("private SQL details");
    }

    @Test
    void returnsSanitizedServerErrorWhenDatabaseConnectionFails() throws Exception {
        when(service.create(any())).thenThrow(new DataAccessResourceFailureException(
                "private data source details", new SQLException("private connection details")));

        String response = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("private data source details", "private connection details");
    }

    @ParameterizedTest
    @MethodSource("invalidCreateBodies")
    void rejectsInvalidCreateBodyBeforeCallingService(String body) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    static Stream<String> invalidCreateBodies() {
        return Stream.of(
                "{}",
                "{\"email\":\"broken\",\"fio\":\"Иван\",\"phone\":\"+79991234567\"}",
                "{\"email\":\" \",\"fio\":\"Иван\",\"phone\":\"+79991234567\"}",
                "{\"email\":\"ivan@example.com\",\"fio\":\" \",\"phone\":\"+79991234567\"}",
                "{\"email\":\"ivan@example.com\",\"fio\":\"Иван\",\"phone\":\" \"}",
                "{\"email\":\"ivan@example.com\",\"fio\":\"Иван\",\"phone\":\"not-a-phone\"}",
                "{\"email\":\"ivan@example.com\",\"fio\":\"" + "x".repeat(256) + "\",\"phone\":\"+79991234567\"}",
                "{\"email\":\"" + "x".repeat(250) + "@example.com\",\"fio\":\"Иван\",\"phone\":\"+79991234567\"}",
                "{\"email\":\"ivan@example.com\",\"fio\":\"Иван\",\"phone\":\"" + "1".repeat(33) + "\"}"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"version\":null}", "{\"version\":-1}", "{\"email\":\"broken\",\"version\":0}",
            "{\"fio\":\" \",\"version\":0}", "{\"fio\":\"\u2003\",\"version\":0}", "{\"phone\":\" \",\"version\":0}"
    })
    void rejectsInvalidUpdateBodyBeforeCallingService(String body) throws Exception {
        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "not-a-number"})
    void rejectsInvalidPathIdentifier(String id) throws Exception {
        mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=-1", "size=0", "size=101", "page=abc", "size=abc", "id=0", "version=-1", "createdFrom=invalid", "createdTo=invalid"})
    void rejectsInvalidSearchAndPagination(String query) throws Exception {
        String[] parameter = query.split("=", 2);

        mockMvc.perform(get(URL).param(parameter[0], parameter[1]))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void returnsBadRequestForRejectedSort() throws Exception {
        when(service.findAll(any(), any())).thenThrow(new IllegalArgumentException("Неизвестное поле сортировки"));

        mockMvc.perform(get(URL).param("sort", "password,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsBadRequestForRejectedDateRange() throws Exception {
        when(service.findAll(any(), any())).thenThrow(new IllegalArgumentException("createdFrom must not be after createdTo"));

        mockMvc.perform(get(URL)
                        .param("createdFrom", "2026-12-01T00:00:00Z")
                        .param("createdTo", "2026-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "fio", "phone"})
    void rejectsSearchFieldsLongerThanStorageLimits(String field) throws Exception {
        mockMvc.perform(get(URL).param(field, "x".repeat(256)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "fio", "phone"})
    void rejectsUpdateFieldsLongerThanStorageLimits(String field) throws Exception {
        String body = "{\"" + field + "\":\"" + "x".repeat(256) + "\",\"version\":0}";

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void rejectsInvalidIdentifierForUpdate() throws Exception {
        mockMvc.perform(put(URL + "/0").contentType(MediaType.APPLICATION_JSON).content("{\"version\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void rejectsInvalidIdentifierForDelete() throws Exception {
        mockMvc.perform(delete(URL + "/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void rejectsUnsupportedRequestMediaType() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content(CREATE_BODY))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"version\":\"invalid\"}", "[]", "null"})
    void returnsBadRequestForMalformedJson(String body) throws Exception {
        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    private static Client client() {
        Client client = new Client();
        client.setId(1L);
        client.setEmail("ivan@example.com");
        client.setFio("Иван Иванов");
        client.setPhone("+79991234567");
        client.setCreatedAt(CREATED_AT);
        client.setVersion(0L);
        return client;
    }
}
