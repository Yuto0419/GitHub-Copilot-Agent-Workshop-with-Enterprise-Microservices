package com.skishop.sales.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Kafka Configuration
 * Configuration class utilizing modern Java 21 features
 */
@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka producer configuration
     * Using Java 21's Map.of() and Text Blocks for more readable configuration
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.ACKS_CONFIG, "all",
            ProducerConfig.RETRIES_CONFIG, 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
            ProducerConfig.LINGER_MS_CONFIG, 10,
            ProducerConfig.BATCH_SIZE_CONFIG, 16384
        ));
    }

    /**
     * KafkaTemplate configuration
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        var template = new KafkaTemplate<>(producerFactory());
        
        // Description for default topic configuration using Java 21's Text Block
        var topicDescription = """
            This KafkaTemplate is used for the following topics:
            - sales.order.created: Order creation event
            - sales.order.updated: Order update event
            - sales.order.cancelled: Order cancellation event
            - sales.payment.completed: Payment completion event
            """;
        
        // Log output (does not affect actual configuration)
        log.debug("Kafka topic configuration: {}", topicDescription);
        
        return template;
    }
}
