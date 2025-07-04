package com.skishop.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Azure SkiShop Frontend Service Application
 * 
 * Main entry point for frontend service
 * Provides customer-facing website and admin management interface
 */
@SpringBootApplication(exclude = {OAuth2ClientAutoConfiguration.class})
@EnableCaching
public class FrontendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendServiceApplication.class, args);
    }
}
