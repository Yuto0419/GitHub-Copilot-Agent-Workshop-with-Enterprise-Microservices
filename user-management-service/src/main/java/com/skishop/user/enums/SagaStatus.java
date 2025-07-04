package com.skishop.user.enums;

/**
 * Orchestration status in the Saga pattern
 * Comprehensive Saga lifecycle management based on design document requirements
 */
public enum SagaStatus {
    
    // Saga initiation and progress
    /**
     * Saga transaction started
     */
    SAGA_STARTED("Saga transaction started"),
    
    /**
     * Saga transaction in progress
     */
    SAGA_IN_PROGRESS("Saga transaction in progress"),
    
    /**
     * Saga step completed
     */
    SAGA_STEP_COMPLETED("Saga step completed"),
    
    /**
     * Saga step failed
     */
    SAGA_STEP_FAILED("Saga step failed"),
    
    // Compensation processing
    /**
     * Compensation processing in progress
     */
    SAGA_COMPENSATING("Compensation processing in progress"),
    
    /**
     * Compensation processing completed
     */
    SAGA_COMPENSATED("Compensation processing completed"),
    
    /**
     * Compensation processing failed
     */
    SAGA_COMPENSATION_FAILED("Compensation processing failed"),
    
    // Final states
    /**
     * Saga transaction successfully completed
     */
    SAGA_COMPLETED("Saga transaction successfully completed"),
    
    /**
     * Saga transaction failed
     */
    SAGA_FAILED("Saga transaction failed"),
    
    /**
     * Saga transaction timed out
     */
    SAGA_TIMEOUT("Saga transaction timed out");

    private final String description;

    SagaStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines if the status is a terminal state
     */
    public boolean isTerminal() {
        return this == SAGA_COMPLETED || 
               this == SAGA_FAILED || 
               this == SAGA_COMPENSATION_FAILED ||
               this == SAGA_TIMEOUT;
    }

    /**
     * Determines if the status indicates success
     */
    public boolean isSuccess() {
        return this == SAGA_COMPLETED;
    }

    /**
     * Determines if the status indicates failure
     */
    public boolean isFailure() {
        return this == SAGA_FAILED || 
               this == SAGA_COMPENSATION_FAILED ||
               this == SAGA_TIMEOUT;
    }

    /**
     * Determines if compensation is needed
     */
    public boolean needsCompensation() {
        return this == SAGA_STEP_FAILED || this == SAGA_COMPENSATING;
    }

    /**
     * Determines if retry is possible
     */
    public boolean isRetryable() {
        return this == SAGA_STEP_FAILED && !isTerminal();
    }
}
