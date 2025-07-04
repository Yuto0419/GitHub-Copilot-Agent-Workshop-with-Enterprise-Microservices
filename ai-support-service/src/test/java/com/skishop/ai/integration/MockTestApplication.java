package com.skishop.ai.integration;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.skishop.ai.config.LangChain4jConfig;
import com.skishop.ai.service.CustomerSupportAssistant;
import com.skishop.ai.service.ProductRecommendationAssistant;
import com.skishop.ai.service.SearchEnhancementAssistant;

import static org.mockito.Mockito.mock;

/**
 * Mocked test-specific application
 * Completely eliminates database dependencies
 */
@SpringBootApplication(
    exclude = {
        // MongoDB auto-configurations
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class,
        MongoAutoConfiguration.class,
        // JDBC/JPA auto-configurations
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
    },
    scanBasePackages = {
        "com.skishop.ai.controller",
        "com.skishop.ai.service",
        "com.skishop.ai.config"
    }
)
@Profile("test")
public class MockTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockTestApplication.class, args);
    }

    /**
     * Mocked ContentRetriever for testing purposes
     */
    @Bean
    @Primary
    public ContentRetriever mockContentRetriever() {
        return mock(ContentRetriever.class);
    }

    /**
     * Mocked LangChain4jConfig for testing purposes
     */
    @Bean
    @Primary
    public LangChain4jConfig mockLangChain4jConfig() {
        return mock(LangChain4jConfig.class);
    }

    /**
     * Mocked CustomerSupportAssistant for testing purposes
     */
    @Bean
    @Primary
    public CustomerSupportAssistant mockCustomerSupportAssistant() {
        return mock(CustomerSupportAssistant.class);
    }

    /**
     * Mocked ProductRecommendationAssistant for testing purposes
     */
    @Bean
    @Primary
    public ProductRecommendationAssistant mockProductRecommendationAssistant() {
        return mock(ProductRecommendationAssistant.class);
    }

    /**
     * Mocked SearchEnhancementAssistant for testing purposes
     */
    @Bean
    @Primary
    public SearchEnhancementAssistant mockSearchEnhancementAssistant() {
        return mock(SearchEnhancementAssistant.class);
    }
}
