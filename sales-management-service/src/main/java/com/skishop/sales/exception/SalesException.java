package com.skishop.sales.exception;

/**
 * Base class for sales operation exceptions
 * Using Java 21 Sealed Classes to clearly define the exception hierarchy
 */
public sealed class SalesException extends RuntimeException 
    permits InvalidOrderStateException, BusinessRuleViolationException, InsufficientStockException {
    
    public SalesException(String message) {
        super(message);
    }

    public SalesException(String message, Throwable cause) {
        super(message, cause);
    }
}
