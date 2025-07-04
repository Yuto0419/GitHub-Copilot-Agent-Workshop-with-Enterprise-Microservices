package com.skishop.ai.config;

import com.skishop.ai.service.CustomerSupportAssistant;
import com.skishop.ai.service.ProductRecommendationAssistant;
import com.skishop.ai.service.SearchEnhancementAssistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * LangChain4j 1.1.0 Configuration for Azure OpenAI
 * 
 * <p>This configuration class provides connection to Azure OpenAI using the latest LangChain4j 1.1.0 API.</p>
 * 
 * <h3>Azure OpenAI Service Guidelines:</h3>
 * <ul>
 *   <li>Use API Key authentication (Managed Identity recommended for production)</li>
 *   <li>Use serviceVersion parameter (changed from apiVersion)</li>
 *   <li>Include error handling and retry functionality</li>
 *   <li>Secure configuration management</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Configuration
public class LangChain4jConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Value("${azure.openai.api-key}")
    private String apiKey;

    @Value("${azure.openai.endpoint}")
    private String endpoint;

    @Value("${azure.openai.deployment-name}")
    private String chatDeploymentName;

    @Value("${azure.openai.embedding-deployment-name}")
    private String embeddingDeploymentName;

    @Value("${azure.openai.api-version:2024-02-15-preview}")
    private String serviceVersion;

    @Value("${azure.openai.temperature:0.7}")
    private Double temperature;

    @Value("${azure.openai.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${azure.openai.timeout:60s}")
    private Duration timeout;

    @Value("${azure.openai.max-retries:3}")
    private Integer maxRetries;

    /**
     * Azure OpenAI Chat Model Configuration
     * 
     * <p>Configure ChatModel using LangChain4j 1.1.0 latest API</p>
     * <p>Logs disabled for security</p>
     * 
     * @return Configured ChatModel instance
     */
    @Bean
    @Primary
    public ChatModel chatLanguageModel() {
        logger.info("Configuring Azure OpenAI Chat Model with deployment: {}", chatDeploymentName);
        
        return AzureOpenAiChatModel.builder()
                .apiKey(apiKey)
                .endpoint(endpoint)
                .deploymentName(chatDeploymentName)
                .serviceVersion(serviceVersion)  // Changed from apiVersion to serviceVersion in LangChain4j 1.1.0
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(timeout)
                .maxRetries(maxRetries)
                .logRequestsAndResponses(false)  // Recommended false for production environment
                .build();
    }

    /**
     * Azure OpenAI Embedding Model Configuration
     * 
     * <p>Model configuration for generating text embeddings</p>
     * 
     * @return Configured EmbeddingModel instance
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        logger.info("Configuring Azure OpenAI Embedding Model with deployment: {}", embeddingDeploymentName);
        
        return AzureOpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .endpoint(endpoint)
                .deploymentName(embeddingDeploymentName)
                .serviceVersion(serviceVersion)  // Changed from apiVersion to serviceVersion in LangChain4j 1.1.0
                .timeout(timeout)
                .maxRetries(maxRetries)
                .logRequestsAndResponses(false)  // Disabled for security
                .build();
    }

    /**
     * Chat Memory Store Configuration
     * 
     * <p>In-memory store for saving conversation history</p>
     * 
     * @return ChatMemoryStore instance
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * Message Window Chat Memory Configuration
     * 
     * <p>Configuration for conversation history management</p>
     * 
     * @return MessageWindowChatMemory instance
     */
    @Bean
    public MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)  // Maximum number of messages to retain
                .chatMemoryStore(chatMemoryStore())
                .build();
    }

    /**
     * Customer Support AI Service
     * 
     * <p>Generate customer support AI service using LangChain4j 1.1.0 AiServices</p>
     * 
     * @return CustomerSupportAssistant instance
     */
    @Bean
    public CustomerSupportAssistant customerSupportAssistant() {
        logger.info("Creating Customer Support Assistant with LangChain4j 1.1.0");
        
        return AiServices.builder(CustomerSupportAssistant.class)
                .chatModel(chatLanguageModel())
                .chatMemory(chatMemory())
                .build();
    }

    /**
     * Product Recommendation AI Service
     * 
     * <p>AI service for product recommendation functionality</p>
     * 
     * @return ProductRecommendationAssistant instance
     */
    @Bean
    public ProductRecommendationAssistant productRecommendationAssistant() {
        logger.info("Creating Product Recommendation Assistant with LangChain4j 1.1.0");
        
        return AiServices.builder(ProductRecommendationAssistant.class)
                .chatModel(chatLanguageModel())
                .build();
    }

    /**
     * Search Enhancement AI Service
     * 
     * <p>AI service for search enhancement functionality</p>
     * 
     * @return SearchEnhancementAssistant instance
     */
    @Bean
    public SearchEnhancementAssistant searchEnhancementAssistant() {
        logger.info("Creating Search Enhancement Assistant with LangChain4j 1.1.0");
        
        return AiServices.builder(SearchEnhancementAssistant.class)
                .chatModel(chatLanguageModel())
                .build();
    }
}
