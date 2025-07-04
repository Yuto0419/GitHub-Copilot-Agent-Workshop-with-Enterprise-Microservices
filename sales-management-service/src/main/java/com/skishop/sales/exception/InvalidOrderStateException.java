package com.skishop.sales.exception;

/**
 * Exception for invalid order state
 * Defined as part of the SalesException Sealed Class hierarchy
 */
public final class InvalidOrderStateException extends SalesException {

    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
