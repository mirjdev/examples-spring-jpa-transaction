package com.mirjdev.examplesspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ExamplesSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamplesSpringApplication.class, args);
    }

}
