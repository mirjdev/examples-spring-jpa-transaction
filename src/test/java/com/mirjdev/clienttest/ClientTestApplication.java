package com.mirjdev.clienttest;

import com.mirjdev.examplesspring.config.JpaConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Минимальное приложение для интеграционных тестов клиентского API.
 *
 * <p>Намеренно расположено вне пакета {@code com.mirjdev.examplesspring},
 * чтобы полноценные тесты приложения не подхватывали этот контекст
 * компонентным сканированием.</p>
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.mirjdev.examplesspring.client")
@EntityScan(basePackages = "com.mirjdev.examplesspring.client")
@EnableJpaRepositories(basePackages = "com.mirjdev.examplesspring.client")
@Import(JpaConfiguration.class)
public class ClientTestApplication {
}
