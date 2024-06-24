package com.mirjdev.examplesspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableScheduling
//@EnableJpaAuditing
@SpringBootApplication
public class ExamplesSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamplesSpringApplication.class, args);
    }

}
