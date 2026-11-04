package com.mirjdev.examplesspring.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий клиентов.
 *
 * <p>{@link JpaSpecificationExecutor} позволяет строить динамические фильтры
 * без раздувания количества derived-методов.</p>
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
