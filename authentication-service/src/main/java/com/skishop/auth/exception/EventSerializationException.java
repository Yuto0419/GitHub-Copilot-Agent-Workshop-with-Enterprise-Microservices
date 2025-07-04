package com.skishop.auth.exception;

/**
 * Exception thrown during event serialization
 */
public class EventSerializationException extends RuntimeException {
    
    public EventSerializationException(String message) {
        super(message);
    }
    
    public EventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
