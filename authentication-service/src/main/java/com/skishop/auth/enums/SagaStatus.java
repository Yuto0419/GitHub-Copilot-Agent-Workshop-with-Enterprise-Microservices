package com.skishop.auth.enums;

/**
 * Orchestration status in the Saga pattern
 */
public enum SagaStatus {
    SAGA_STARTED("Saga transaction started"),
    SAGA_IN_PROGRESS("Saga transaction in progress"),
    SAGA_STEP_COMPLETED("Saga step completed"),
    SAGA_STEP_FAILED("Saga step failed"),
    SAGA_COMPENSATING("Compensation process in progress"),
    SAGA_COMPENSATED("Compensation process completed"),
    SAGA_COMPENSATION_FAILED("Compensation process failed"),
    SAGA_COMPLETED("Saga transaction completed successfully"),
    SAGA_FAILED("Saga transaction failed"),
    SAGA_TIMEOUT("Saga transaction timeout");
    
    private final String description;
    
    SagaStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
