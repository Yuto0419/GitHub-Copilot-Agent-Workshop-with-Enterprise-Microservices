package com.skishop.user.enums;

/**
 * Processing Status (for feedback from user management service to authentication service)
 */
public enum ProcessingStatus {
    
    /**
     * Processing successful
     */
    SUCCESS("Processing successful"),
    
    /**
     * Processing failed
     */
    FAILED("Processing failed"),
    
    /**
     * Compensation processing successful
     */
    COMPENSATION_SUCCESS("Compensation processing successful"),
    
    /**
     * Compensation processing failed
     */
    COMPENSATION_FAILED("Compensation processing failed");

    private final String description;

    ProcessingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
