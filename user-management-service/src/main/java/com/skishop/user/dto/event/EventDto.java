package com.skishop.user.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

/**
 * Event DTO (integrated, generic type supported)
 * Event schema implementation according to design specifications
 * Type-safe payload handling
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto<T> {
    
    /**
     * Unique event ID (UUID string)
     */
    @JsonProperty("eventId")
    private String eventId;
    
    /**
     * Event type
     * Example: USER_REGISTERED, USER_DELETED, USER_MANAGEMENT_STATUS
     */
    @JsonProperty("eventType")
    private String eventType;
    
    /**
     * Event occurrence timestamp (ISO-8601 format)
     */
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
    
    /**
     * Event schema version
     */
    @JsonProperty("version")
    @Builder.Default
    private String version = "1.0";
    
    /**
     * Event producer service
     * Example: authentication-service, user-management-service
     */
    @JsonProperty("producer")
    private String producer;
    
    /**
     * Event payload (type-safe with generics)
     */
    @JsonProperty("payload")
    private T payload;
    
    /**
     * Correlation ID for related request (UUID string)
     */
    @JsonProperty("correlationId")
    private String correlationId;
    
    /**
     * Saga transaction ID (UUID string)
     */
    @JsonProperty("sagaId")
    private String sagaId;
    
    /**
     * Retry count
     */
    @JsonProperty("retry")
    @Builder.Default
    private Integer retry = 0;
    
    /**
     * Additional metadata (optional)
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
