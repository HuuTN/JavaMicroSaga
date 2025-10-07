package com.demo.eventsourcing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Event Sourcing Service.
 * This service provides event store capabilities with snapshots and temporal queries.
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class EventSourcingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventSourcingServiceApplication.class, args);
    }
}