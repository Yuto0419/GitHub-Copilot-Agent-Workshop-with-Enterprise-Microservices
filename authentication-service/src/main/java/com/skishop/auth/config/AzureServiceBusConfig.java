package com.skishop.auth.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Azure Service Bus configuration class
 *
 * Configuration for using Azure Service Bus for event propagation in production environment
 * Implements secure authentication using managed identity
 */
@Configuration
@ConditionalOnProperty(
    prefix = "skishop.runtime.azure-servicebus", 
    name = "enabled", 
    havingValue = "true"
)
@Profile({"production", "staging"})
@Slf4j
public class AzureServiceBusConfig {

    @Value("${spring.cloud.azure.servicebus.namespace}")
    private String serviceBusNamespace;

    @Value("${skishop.runtime.azure-servicebus.topic-name}")
    private String topicName;

    @Value("${skishop.runtime.azure-servicebus.subscription-name}")
    private String subscriptionName;

    @Value("${skishop.runtime.azure-servicebus.status-feedback-topic}")
    private String statusFeedbackTopic;

    @Value("${spring.cloud.azure.servicebus.connection-string:}")
    private String connectionString;

    /**
     * ServiceBus Sender Client for event sending
     * Used to send messages to topic
     */
    @Bean
    public ServiceBusSenderClient eventSenderClient() {
        log.info("Creating Azure Service Bus Sender Client for topic: {}", topicName);
        
        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder();
        
        // Use connection string if set, otherwise use managed identity
        if (connectionString != null && !connectionString.isEmpty()) {
            clientBuilder.connectionString(connectionString);
            log.debug("Using connection string for Service Bus authentication");
        } else {
            clientBuilder
                .fullyQualifiedNamespace(serviceBusNamespace + ".servicebus.windows.net")
                .credential(new DefaultAzureCredentialBuilder().build());
            log.debug("Using Managed Identity for Service Bus authentication");
        }
        
        return clientBuilder
            .sender()
            .topicName(topicName)
            .buildClient();
    }

    /**
     * ServiceBus Processor Client for receiving status feedback
     * Receives status updates from other services
     */
    @Bean
    public ServiceBusProcessorClient statusFeedbackProcessorClient() {
        log.info("Creating Azure Service Bus Processor Client for subscription: {}", subscriptionName);
        
        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder();
        
        // Use connection string if set, otherwise use managed identity
        if (connectionString != null && !connectionString.isEmpty()) {
            clientBuilder.connectionString(connectionString);
        } else {
            clientBuilder
                .fullyQualifiedNamespace(serviceBusNamespace + ".servicebus.windows.net")
                .credential(new DefaultAzureCredentialBuilder().build());
        }
        
        return clientBuilder
            .processor()
            .topicName(statusFeedbackTopic)
            .subscriptionName(subscriptionName)
            .processMessage(message -> {
                log.debug("Received status feedback message: {}", message.getMessage().getBody().toString());
                // Message processing is delegated to StatusFeedbackService
            })
            .processError(context -> {
                log.error("Error processing status feedback message: {}", 
                    context.getException().getMessage(), context.getException());
            })
            .buildProcessorClient();
    }

    /**
     * ServiceBus Sender Client for sending status feedback
     * Used to send status updates to other services
     */
    @Bean
    public ServiceBusSenderClient statusFeedbackSenderClient() {
        log.info("Creating Azure Service Bus Status Feedback Sender Client for topic: {}", 
            statusFeedbackTopic);
        
        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder();
        
        if (connectionString != null && !connectionString.isEmpty()) {
            clientBuilder.connectionString(connectionString);
        } else {
            clientBuilder
                .fullyQualifiedNamespace(serviceBusNamespace + ".servicebus.windows.net")
                .credential(new DefaultAzureCredentialBuilder().build());
        }
        
        return clientBuilder
            .sender()
            .topicName(statusFeedbackTopic)
            .buildClient();
    }
}
