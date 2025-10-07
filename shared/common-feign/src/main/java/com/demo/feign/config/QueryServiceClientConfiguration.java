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
 * Configuration for Query Service Feign client.
 * Includes circuit breaker, retry, and caching configuration.
 */
@Configuration
public class QueryServiceClientConfiguration {

    /**
     * Feign logger level for queries (more verbose for debugging)
     */
    @Bean
    Logger.Level queryFeignLoggerLevel() {
        return Logger.Level.HEADERS;
    }

    /**
     * Request timeout configuration for queries
     */
    @Bean
    Request.Options queryRequestOptions() {
        // Feign Request.Options accepts millisecond int overloads in this Feign version
        return new Request.Options(
            5000, // connect timeout in ms
            30000 // read timeout in ms
        );
    }

    /**
     * Retry configuration for queries (more aggressive)
     */
    @Bean
    Retryer queryRetryer() {
        return new Retryer.Default(
            50,    // initial interval
            500,   // max interval
            5      // max attempts (more retries for read operations)
        );
    }

    /**
     * Request interceptor for query operations
     */
    @Bean
    RequestInterceptor queryCorrelationIdInterceptor() {
        return template -> {
            // Add correlation ID from MDC if present
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                template.header("X-Correlation-ID", correlationId);
            }
            
            // Add service name header
            template.header("X-Client-Service", "query-service-client");
            
            // Add accept header
            template.header("Accept", "application/json");
            
            // Add cache control for read operations
            template.header("Cache-Control", "max-age=300"); // 5 minutes
        };
    }

    /**
     * Custom error decoder for query operations
     */
    @Bean
    ErrorDecoder queryErrorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Circuit breaker configuration for Query Service
     */
    @Bean
    CircuitBreaker queryServiceCircuitBreaker() {
        return CircuitBreaker.ofDefaults("query-service");
    }

    /**
     * Feign builder with resilience4j decorators for queries
     */
    @Bean
    Resilience4jFeign.Builder queryResilience4jFeignBuilder(CircuitBreaker queryServiceCircuitBreaker) {
        FeignDecorators decorators = FeignDecorators.builder()
            .withCircuitBreaker(queryServiceCircuitBreaker)
            .build();
        
        return Resilience4jFeign.builder(decorators);
    }
}