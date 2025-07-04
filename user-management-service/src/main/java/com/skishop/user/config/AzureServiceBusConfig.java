package com.skishop.user.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.skishop.user.service.azure.AzureServiceBusEventReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Azure Service Bus configuration class
 * 
 * Configuration for event propagation using Azure Service Bus in production environment
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

    private final SkishopRuntimeProperties runtimeProperties;

    @Value("${spring.cloud.azure.servicebus.connection-string:}")
    private String connectionString;
    
    public AzureServiceBusConfig(SkishopRuntimeProperties runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    /**
     * ServiceBus Processor Client for receiving events
     * Receives and processes events from the authentication service
     */
    @Bean
    public ServiceBusProcessorClient eventProcessorClient(
            @Autowired ObjectProvider<AzureServiceBusEventReceiver> eventReceiverProvider) {
        String topicName = runtimeProperties.getAzureServicebus().getTopicName();
        String subscriptionName = runtimeProperties.getAzureServicebus().getSubscriptionName();
        int prefetchCount = runtimeProperties.getAzureServicebus().getPrefetchCount();
        int maxConcurrentCalls = runtimeProperties.getAzureServicebus().getMaxConcurrentCalls();
        
        log.info("Creating Azure Service Bus Processor Client for subscription: {}", subscriptionName);
        
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
        
        // Initially create processor without callback
        // Callback is set after AzureServiceBusEventReceiver is injected
        return clientBuilder
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .disableAutoComplete()  // Allow explicit completion or error handling
            .maxConcurrentCalls(maxConcurrentCalls)  // Control concurrency
            .prefetchCount(prefetchCount)      // Performance optimization
            .processMessage(context -> {
                // Use method from AzureServiceBusEventReceiver after injection
                AzureServiceBusEventReceiver receiver = eventReceiverProvider.getIfAvailable();
                if (receiver != null) {
                    receiver.processMessage(context);
                } else {
                    log.warn("AzureServiceBusEventReceiver not available, message will not be processed");
                    context.abandon();
                }
            })
            .processError(context -> {
                // Use method from AzureServiceBusEventReceiver after injection
                AzureServiceBusEventReceiver receiver = eventReceiverProvider.getIfAvailable();
                if (receiver != null) {
                    receiver.processError(context);
                } else {
                    log.error("AzureServiceBusEventReceiver not available, error will not be processed: {}", 
                        context.getException().getMessage(), context.getException());
                }
            })
            .buildProcessorClient();
    }

    /**
     * ServiceBus Sender Client for sending status feedback
     * Used to send status updates to the authentication service
     */
    @Bean
    public ServiceBusSenderClient statusFeedbackSenderClient() {
        String statusFeedbackTopic = runtimeProperties.getAzureServicebus().getStatusFeedbackTopic();
        
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
