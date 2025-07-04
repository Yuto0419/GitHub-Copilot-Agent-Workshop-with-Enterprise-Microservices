package com.skishop.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Sales Management Service
 * 
 * Features:
 * - Order processing and management
 * - Sales analysis and reporting
 * - Returns and exchanges processing
 * - Shipping arrangement and tracking
 * - Sales history management
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.skishop.sales.repository.jpa")
public class SalesManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesManagementApplication.class, args);
    }
}
