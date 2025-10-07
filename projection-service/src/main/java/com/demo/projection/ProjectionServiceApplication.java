package com.demo.projection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Projection Service.
 * This service handles materialized views and aggregated projections in the CQRS pattern.
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
public class ProjectionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectionServiceApplication.class, args);
    }
}