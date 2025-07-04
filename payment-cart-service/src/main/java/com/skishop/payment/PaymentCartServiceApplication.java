package com.skishop.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Payment and Cart Service Main Application
 */
@SpringBootApplication
@EnableCaching
@EnableJpaRepositories
@EnableScheduling
public class PaymentCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentCartServiceApplication.class, args);
    }
}
