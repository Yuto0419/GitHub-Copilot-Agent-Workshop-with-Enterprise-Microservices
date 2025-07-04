package com.skishop.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Authentication Service Main Application
 * 
 * Authentication and authorization service for the ski shop e-commerce platform
 * - User authentication (login/logout)
 * - JWT token generation and validation
 * - OAuth integration (Azure Entra ID, Google, Facebook, LINE)
 * - Multi-factor authentication (MFA)
 * - Session management and security
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class AuthenticationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }
}
