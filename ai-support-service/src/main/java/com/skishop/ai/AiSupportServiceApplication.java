package com.skishop.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI Support Service Main Application
 * 
 * AI support service for ski shop using Spring Boot 3.x + Java 21 + LangChain4j 1.1.0 + Azure OpenAI
 * 
 * Main Features:
 * - Chatbot (Product recommendations, Technical advice)
 * - Search enhancement (RAG)
 * - Customer support
 */
@SpringBootApplication
@EnableCaching      // Enable Redis caching
@EnableMongoRepositories  // Enable MongoDB repositories
@EnableAsync        // Enable async processing
@EnableScheduling   // Enable scheduled processing
public class AiSupportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSupportServiceApplication.class, args);
    }
}
