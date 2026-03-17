package com.rotopay.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.rotopay.expensetracker",
		"com.rotopay.expensetracker.api",
		"com.rotopay.expensetracker.api.v1",
		"com.rotopay.expensetracker.api.v1.controller",
		"com.rotopay.expensetracker.service",
		"com.rotopay.expensetracker.repository",
		"com.rotopay.expensetracker.config"
})
@EnableAsync
@EnableJpaRepositories(basePackages = "com.rotopay.expensetracker.repository")
public class ExpensetrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpensetrackerApplication.class, args);

	}

}
