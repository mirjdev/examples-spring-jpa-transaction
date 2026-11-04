package com.mirjdev.examplesspring.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientEntityTest {

    @Test
    void equalsAndHashCode_areBasedOnId() {
        Client first = Client.builder().id(1L).email("first@example.com").build();
        Client sameId = Client.builder().id(1L).email("other@example.com").build();
        Client otherId = Client.builder().id(2L).email("first@example.com").build();

        assertThat(first).isEqualTo(sameId).hasSameHashCodeAs(sameId);
        assertThat(first).isNotEqualTo(otherId);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("not a client");
    }

    @Test
    void newEntity_isNotEqualToPersistedEntity() {
        Client transientClient = new Client();
        Client persistedClient = Client.builder().id(1L).build();

        assertThat(transientClient).isNotEqualTo(persistedClient);
    }
}
