package com.skishop.sales.exception;

/**
 * Exception for business rule violations
 */
public final class BusinessRuleViolationException extends SalesException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
