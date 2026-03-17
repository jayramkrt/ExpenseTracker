package com.rotopay.expensetracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * DispatcherServlet Configuration for Spring Boot 4.0.3+
 *
 * IMPORTANT: Do NOT manually register DispatcherServlet - Spring Boot does this automatically.
 * This class is kept for future customizations if needed.
 *
 * Most configuration is now handled by:
 * - WebMvcConfig.java (static resources)
 * - CorsConfig.java (CORS)
 * - WebClientConfig.java (HTTP clients)
 */
@Configuration
public class DispatcherServletConfig implements WebMvcConfigurer {

    // Spring Boot 4.0.3 handles DispatcherServlet configuration automatically
    // No custom configuration needed here for basic functionality
}