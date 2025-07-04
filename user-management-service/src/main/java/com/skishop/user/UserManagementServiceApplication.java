package com.skishop.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the User Management Service
 * 
 * This service provides the following features:
 * <ul>
 * <li>User registration and authentication</li>
 * <li>User profile management</li>
 * <li>Permission management</li>
 * <li>User settings management</li>
 * <li>Activity tracking</li>
 * </ul>
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableJpaAuditing
public class UserManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplication.class, args);
    }
}
