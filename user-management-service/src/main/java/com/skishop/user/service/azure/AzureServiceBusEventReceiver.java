package com.skishop.user.service.azure;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.user.dto.event.EventDto;
import com.skishop.user.service.EventConsumerService;
import com.skishop.user.service.metrics.MetricsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Azure Service Bus event receiving service
 * 
 * Receives events from authentication service via Azure Service Bus
 * Automatic message deserialization and error handling
 */
@Service
@ConditionalOnProperty(
    prefix = "skishop.runtime.azure-servicebus", 
    name = "enabled", 
    havingValue = "true"
)
@RequiredArgsConstructor
@Slf4j
public class AzureServiceBusEventReceiver {

    private final ServiceBusProcessorClient eventProcessorClient;
    private final EventConsumerService eventConsumerService;
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;

    /**
     * Start Service Bus Processor
     */
    @PostConstruct
    public void startReceiving() {
        log.info("Starting Azure Service Bus Event Processor");
        
        // Start the processor
        // Note: Handler is already configured in AzureServiceBusConfig
        if (!eventProcessorClient.isRunning()) {
            eventProcessorClient.start();
            log.info("Azure Service Bus Event Processor started successfully");
        } else {
            log.info("Azure Service Bus Event Processor is already running");
        }
    }

    /**
     * Stop Service Bus Processor
     */
    @PreDestroy
    public void stopReceiving() {
        log.info("Stopping Azure Service Bus Event Processor");
        
        if (eventProcessorClient != null) {
            eventProcessorClient.close();
        }
        
        log.info("Azure Service Bus Event Processor stopped");
    }

    /**
     * Process Service Bus message
     * 
     * @param context Service Bus message context
     */
    public void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        long startTime = System.currentTimeMillis();
        String messageId = message.getMessageId();
        String correlationId = message.getCorrelationId();
        
        try {
            log.debug("Processing event message: messageId={}, correlationId={}", 
                messageId, correlationId);

            // Deserialize message body to EventDto
            String messageBody = message.getBody().toString();
            EventDto<?> event = objectMapper.readValue(messageBody, EventDto.class);

            // Process with event consumer service
            eventConsumerService.processEvent(event);

            // Complete the message
            context.complete();
            
            // Measure processing time and record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordEventProcessed(
                event.getEventType(), 
                "azure-servicebus", 
                processingTime);
            
            log.info("Successfully processed event message: eventId={}, eventType={}, processingTime={}ms", 
                event.getEventId(), event.getEventType(), processingTime);

        } catch (Exception e) {
            log.error("Failed to process event message: messageId={}, error={}", 
                messageId, e.getMessage(), e);
            
            // Check retry count
            long deliveryCount = message.getDeliveryCount();
            if (deliveryCount >= 3) {
                // Send to dead letter queue when maximum retry count is reached
                DeadLetterOptions options = new DeadLetterOptions()
                    .setDeadLetterReason("MaxRetryExceeded")
                    .setDeadLetterErrorDescription("Maximum retry attempts exceeded: " + e.getMessage());
                context.deadLetter(options);
                
                // Record metrics
                metricsService.recordMessageBrokerError(
                    "azure-servicebus", 
                    "MaxRetryExceeded", 
                    "Maximum retry attempts exceeded for message: " + messageId);
                
                log.warn("Message sent to dead letter queue after {} attempts: messageId={}", 
                    deliveryCount, messageId);
            } else {
                // Abandon message for retry
                context.abandon();
                
                log.info("Message abandoned for retry (attempt {}/3): messageId={}", 
                    deliveryCount + 1, messageId);
            }
            
            // Record error metrics
            String errorType = e.getClass().getSimpleName();
            metricsService.recordEventFailure(
                "event-processing", 
                "azure-servicebus", 
                System.currentTimeMillis() - startTime,
                errorType,
                e.getMessage());
        }
    }

    /**
     * Process Service Bus error
     * 
     * @param context Error context
     */
    public void processError(ServiceBusErrorContext context) {
        log.error("Azure Service Bus error occurred: {}", 
            context.getException().getMessage(), context.getException());

        // Record error metrics
        String errorType = context.getException().getClass().getSimpleName();
        String errorMessage = context.getException().getMessage();
        metricsService.recordMessageBrokerError("azure-servicebus", errorType, errorMessage);
    }

    /**
     * Check processor health
     * 
     * @return true if processor is running
     */
    public boolean isHealthy() {
        return eventProcessorClient != null && 
               eventProcessorClient.isRunning();
    }
}
