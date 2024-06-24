package com.mirjdev.examplesspring.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan({
        "com.mirjdev.examplesspring.entity",
        "com.mirjdev.examplesspring.entity.repository",
}
)
public class JpaTestConfiguration {
}
