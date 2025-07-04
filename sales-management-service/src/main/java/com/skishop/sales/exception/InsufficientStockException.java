package com.skishop.sales.exception;

/**
 * Exception for insufficient stock
 */
public final class InsufficientStockException extends SalesException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
