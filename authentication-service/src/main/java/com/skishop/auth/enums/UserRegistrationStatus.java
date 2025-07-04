package com.skishop.auth.enums;

/**
 * User registration status definitions in accordance with the design document
 */
public enum UserRegistrationStatus {
    // Authentication service statuses
    PENDING_REGISTRATION("User registration request received, processing started in authentication service"),
    ACCOUNT_CREATED("User account creation completed in authentication service"),
    EVENT_PUBLISHED("User registration event published successfully"),
    EVENT_PUBLISH_FAILED("Failed to publish user registration event"),
    PENDING_USER_MANAGEMENT("Waiting for processing in user management service"),
    REGISTRATION_COMPLETED("User registration process completed"),
    REGISTRATION_FAILED("User registration process failed"),
    COMPENSATION_REQUIRED("Compensation process required"),
    COMPENSATED("Compensation process completed"),

    // User management service statuses
    EVENT_RECEIVED("User registration event received"),
    VALIDATION_IN_PROGRESS("Validating event data"),
    VALIDATION_PASSED("Event data validation succeeded"),
    VALIDATION_FAILED("Event data validation failed"),
    PROFILE_CREATION_IN_PROGRESS("Creating user profile"),
    PROFILE_CREATED("User profile creation succeeded"),
    PROFILE_CREATION_FAILED("User profile creation failed"),
    DUPLICATE_USER_DETECTED("Duplicate user detected"),
    PROCESSING_TIMEOUT("Processing timeout");
    
    private final String description;
    
    UserRegistrationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
