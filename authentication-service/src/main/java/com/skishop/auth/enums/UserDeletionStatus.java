package com.skishop.auth.enums;

/**
 * User deletion status definitions in accordance with the design document
 */
public enum UserDeletionStatus {
    // Authentication service statuses
    PENDING_DELETION("User deletion request received, processing started"),
    DELETION_AUTHORIZED("Deletion authorization confirmed"),
    ACCOUNT_SOFT_DELETED("Account soft-deleted in authentication service"),
    DELETION_EVENT_PUBLISHED("User deletion event published successfully"),
    DELETION_EVENT_PUBLISH_FAILED("Failed to publish user deletion event"),
    PENDING_USER_MANAGEMENT_DELETION("Waiting for deletion processing in user management service"),
    DELETION_COMPLETED("User deletion process completed"),
    DELETION_FAILED("User deletion process failed"),
    DELETION_ROLLBACK_REQUIRED("Rollback of deletion required"),
    DELETION_ROLLED_BACK("Deletion rollback completed"),

    // User management service statuses
    DELETION_EVENT_RECEIVED("User deletion event received"),
    DELETION_VALIDATION_IN_PROGRESS("Validating deletion event data"),
    DELETION_VALIDATION_PASSED("Deletion event data validation succeeded"),
    DELETION_VALIDATION_FAILED("Deletion event data validation failed"),
    PROFILE_DELETION_IN_PROGRESS("Deleting user profile"),
    RELATED_DATA_CLEANUP_IN_PROGRESS("Cleaning up related data"),
    PROFILE_DELETED("User profile deletion succeeded"),
    PROFILE_DELETION_FAILED("User profile deletion failed"),
    USER_NOT_FOUND("User to be deleted not found"),
    DELETION_TIMEOUT("Deletion process timeout");
    
    private final String description;
    
    UserDeletionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
