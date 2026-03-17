package com.rotopay.expensetracker.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web Client Configuration for Spring Boot 4.0.3+
 * Provides RestTemplate and ObjectMapper beans for HTTP requests and JSON processing.
 */
@Configuration
public class WebClientConfig {

    /**
     * Create RestTemplate bean with timeout configuration.
     * Used by OllamaClientService for LLM API calls.
     *
     * Compatible with Spring Boot 4.0.3+
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        return restTemplate;
    }

    /**
     * Configure HTTP request factory with timeouts.
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10 seconds
        factory.setReadTimeout(30000);     // 30 seconds
        return factory;
    }

    /**
     * Create ObjectMapper bean for JSON processing.
     * Used by OllamaClientService for parsing JSON responses.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}