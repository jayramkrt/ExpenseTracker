package com.rotopay.expensetracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA repositories and auditing.
 * Enables repository scanning and JPA auditing features.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.expensetracker.expensetracker.repository")
@EnableJpaAuditing
public class RepositoryConfig {
}
