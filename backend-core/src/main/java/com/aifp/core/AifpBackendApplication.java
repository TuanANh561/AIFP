package com.aifp.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AifpBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AifpBackendApplication.class, args);
    }
}

