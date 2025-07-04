package com.skishop.auth.service.azure;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.auth.dto.EventDto;
import com.skishop.auth.service.StatusFeedbackService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Azure Service Bus Status Feedback Receiver Service
 * 
 * Receives status updates from other services via Azure Service Bus
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
public class AzureServiceBusStatusFeedbackReceiver {

    private final ServiceBusProcessorClient statusFeedbackProcessorClient;
    private final StatusFeedbackService statusFeedbackService;
    private final ObjectMapper objectMapper;

    /**
     * Start Service Bus Processor
     */
    @PostConstruct
    public void startReceiving() {
        log.info("Starting Azure Service Bus Status Feedback Processor");
        
        // Set up message processing handler
        statusFeedbackProcessorClient.start();
        
        log.info("Azure Service Bus Status Feedback Processor started successfully");
    }

    /**
     * Stop Service Bus Processor
     */
    @PreDestroy
    public void stopReceiving() {
        log.info("Stopping Azure Service Bus Status Feedback Processor");
        
        if (statusFeedbackProcessorClient != null) {
            statusFeedbackProcessorClient.close();
        }
        
        log.info("Azure Service Bus Status Feedback Processor stopped");
    }

    /**
     * Process Service Bus messages
     * 
     * @param context Service Bus message context
     */
    public void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        
        try {
            log.debug("Processing status feedback message: messageId={}, correlationId={}", 
                message.getMessageId(), message.getCorrelationId());

            // Deserialize message body to EventDto
            String messageBody = message.getBody().toString();
            EventDto statusEvent = objectMapper.readValue(messageBody, EventDto.class);

            // Process with status feedback service
            statusFeedbackService.processStatusFeedback(statusEvent);

            // Complete the message
            context.complete();
            
            log.info("Successfully processed status feedback message: eventId={}, eventType={}", 
                statusEvent.getEventId(), statusEvent.getEventType());

        } catch (Exception e) {
            log.error("Failed to process status feedback message: messageId={}, error={}", 
                message.getMessageId(), e.getMessage(), e);
            
            // Send message to dead letter queue
            context.deadLetter(new DeadLetterOptions()
                .setDeadLetterErrorDescription("Failed to process status feedback")
                .setDeadLetterReason("ProcessingError"));
        }
    }

    /**
     * Process Service Bus errors
     * 
     * @param context Error context
     */
    public void processError(ServiceBusErrorContext context) {
        log.error("Azure Service Bus error occurred: {}", 
            context.getException().getMessage(), context.getException());
        
        // Record error metrics
        // TODO: Implement metrics collection
    }

    /**
     * Check processor health
     * 
     * @return true if processor is running
     */
    public boolean isHealthy() {
        return statusFeedbackProcessorClient != null && 
               statusFeedbackProcessorClient.isRunning();
    }
}
