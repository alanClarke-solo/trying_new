package com.example.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJdbcRepositories(basePackages = "com.example.inventory.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Spring Boot autoconfiguration handles most of the database setup
    // This class enables Spring Data JDBC repositories and transaction management
}