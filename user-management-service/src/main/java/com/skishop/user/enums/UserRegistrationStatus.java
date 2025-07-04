package com.skishop.user.enums;

/**
 * User registration status (User Management Service side)
 * Status definitions according to design specifications
 */
public enum UserRegistrationStatus {
    EVENT_RECEIVED("Event Received"),
    VALIDATION_IN_PROGRESS("Validation in Progress"),
    VALIDATION_PASSED("Validation Passed"),
    VALIDATION_FAILED("Validation Failed"),
    PROFILE_CREATION_IN_PROGRESS("Profile Creation in Progress"),
    PROFILE_CREATED("Profile Created Successfully"),
    PROFILE_CREATION_FAILED("Profile Creation Failed"),
    DUPLICATE_USER_DETECTED("Duplicate User Detected"),
    PROCESSING_TIMEOUT("Processing Timeout");
    
    private final String description;
    
    UserRegistrationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
