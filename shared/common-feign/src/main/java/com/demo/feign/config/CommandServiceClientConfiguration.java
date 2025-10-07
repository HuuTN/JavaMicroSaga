package com.demo.feign.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Command Service Feign client.
 * Includes circuit breaker, retry, and logging configuration.
 */
@Configuration
public class CommandServiceClientConfiguration {

    /**
     * Feign logger level
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Request timeout configuration
     */
    @Bean
    Request.Options requestOptions() {
        // Feign Request.Options accepts millisecond int overloads in this Feign version
        return new Request.Options(
            10000, // connect timeout ms
            60000  // read timeout ms
        );
    }

    /**
     * Retry configuration
     */
    @Bean
    Retryer retryer() {
        return new Retryer.Default(
            100,   // initial interval
            1000,  // max interval
            3      // max attempts
        );
    }

    /**
     * Request interceptor to add correlation ID and other headers
     */
    @Bean
    RequestInterceptor correlationIdInterceptor() {
        return template -> {
            // Add correlation ID from MDC if present
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                template.header("X-Correlation-ID", correlationId);
            }
            
            // Add service name header
            template.header("X-Client-Service", "command-service-client");
            
            // Add accept header
            template.header("Accept", "application/json");
            template.header("Content-Type", "application/json");
        };
    }

    /**
     * Custom error decoder
     */
    @Bean
    ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Circuit breaker configuration for Command Service
     */
    @Bean
    CircuitBreaker commandServiceCircuitBreaker() {
        return CircuitBreaker.ofDefaults("command-service");
    }

    /**
     * Feign builder with resilience4j decorators
     */
    @Bean
    Resilience4jFeign.Builder resilience4jFeignBuilder(CircuitBreaker circuitBreaker) {
        FeignDecorators decorators = FeignDecorators.builder()
            .withCircuitBreaker(circuitBreaker)
            .build();
        
        return Resilience4jFeign.builder(decorators);
    }
}