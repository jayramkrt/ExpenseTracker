package com.rotopay.expensetracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * Web MVC Configuration for Spring Boot 4.0.3+
 * Configures static resource handling.
 *
 * IMPORTANT: API paths (/api/**) should NOT be handled as static resources.
 * Controllers will handle /api/** requests.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

//    @Override
//    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
//        configurer. .disable();  // Don't handle everything as static
//    }

    /**
     * Configure resource handlers for static files.
     *
     * Key: Use addResourceLocations() with files, not directories.
     * This prevents Spring from treating everything as a static resource.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Disable default static resource handling for everything
        // This prevents /** from catching API requests

        // Handle CSS files
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600);

        // Handle JavaScript files
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600);

        // Handle image files
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);

        // Handle favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .setCachePeriod(3600);

        // Handle index.html for SPA
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/index.html")
                .setCachePeriod(0);  // Don't cache HTML

        // Handle static folder explicitly
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        // CRITICAL: Do NOT add "/**" handler - it will catch API requests!
        // Spring will automatically handle any remaining static files
        // but /api/** requests will reach controllers first
    }
}