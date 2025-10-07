package com.demo.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main application class for the Query Service.
 * This service handles all read operations in the CQRS pattern.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.demo.feign.client")
@EnableKafka
public class QueryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryServiceApplication.class, args);
    }
}