package com.rotopay.expensetracker.config;
import org.springframework.beans.factory.annotation.Value;
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
     * Read timeout for Ollama LLM calls.
     * Local inference (e.g. qwen2.5:7b) can take 60-120+ seconds.
     * Default: 300000ms (5 minutes). Override via pfa.llm.timeout in application.properties.
     */
    @Value("${pfa.llm.timeout:300000}")
    private int llmReadTimeoutMs;

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
        factory.setConnectTimeout(10000);      // 10 seconds to connect
        factory.setReadTimeout(llmReadTimeoutMs); // configurable, default 5 minutes for local LLM
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