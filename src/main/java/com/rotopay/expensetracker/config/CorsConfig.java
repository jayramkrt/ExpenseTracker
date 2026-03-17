package com.rotopay.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * CORS Configuration for Spring Boot 4.0.3+
 * Enables Cross-Origin Resource Sharing for frontend requests.
 *
 * Without this, frontend running on port 3000/5173 cannot access backend on port 8080.
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS globally for all API endpoints.
     * Allows requests from frontend applications.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Apply to all endpoints
                        .allowedOrigins(
                                "http://localhost:3000",      // React dev server (port 3000)
                                "http://localhost:5173",      // Vite dev server (port 5173)
                                "http://127.0.0.1:3000",
                                "http://127.0.0.1:5173",
                                "http://localhost:8080"       // Local testing
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")  // Allow all headers
                        .allowCredentials(true)  // Allow cookies/credentials
                        .maxAge(3600);  // Cache preflight for 1 hour
            }
        };
    }

    /**
     * Alternative: CORS configuration source bean.
     * Useful for more granular control.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://127.0.0.1:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:5173");
        configuration.addAllowedOrigin("http://localhost:8080");

        // Allowed HTTP methods
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("PATCH");

        // Allowed headers
        configuration.addAllowedHeader("*");

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Set exposed headers (headers that frontend can read)
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Content-Type");
        configuration.addExposedHeader("X-Total-Count");  // For pagination

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}