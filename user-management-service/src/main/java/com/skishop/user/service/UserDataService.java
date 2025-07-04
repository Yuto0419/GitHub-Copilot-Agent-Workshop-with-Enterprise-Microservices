package com.skishop.user.service;

import com.skishop.user.entity.User;
import com.skishop.user.repository.UserActivityRepository;
import com.skishop.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * User data management service
 * Handles data cleanup, validation, and related data processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataService {

    private final UserActivityRepository userActivityRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * User data cleanup
     */
    @Transactional
    public void cleanupUserData(UUID userId, CleanupType type, String reason) {
        log.info("Starting user data cleanup: userId={}, type={}, reason={}", userId, type, reason);
        
        switch (type) {
            case SOFT_DELETE -> performSoftDeleteCleanup(userId, reason);
            case HARD_DELETE -> performHardDeleteCleanup(userId, reason);
            case EVENT_DRIVEN -> performEventDrivenCleanup(userId, reason);
            default -> throw new IllegalArgumentException("Unknown cleanup type: " + type);
        }
        
        log.info("User data cleanup completed: userId={}, type={}", userId, type);
    }

    /**
     * Delete user preferences
     */
    @Transactional
    public int deleteUserPreferences(UUID userId) {
        log.info("Starting user preferences deletion: userId={}", userId);
        
        int deletedCount = userPreferenceRepository.deleteByUserId(userId);
        
        log.info("User preferences deletion completed: userId={}, deletedCount={}", userId, deletedCount);
        return deletedCount;
    }

    /**
     * Delete user activities
     */
    @Transactional
    public int deleteUserActivities(UUID userId) {
        log.info("User activities deletion started: userId={}", userId);
        
        int deletedCount = userActivityRepository.deleteByUserId(userId);
        
        log.info("User activities deletion completed: userId={}, deletedCount={}", userId, deletedCount);
        return deletedCount;
    }

    /**
     * Delete user sessions (for future expansion)
     */
    @Transactional
    public int deleteUserSessions(UUID userId) {
        log.info("User sessions deletion: userId={}", userId);
        // TODO: Implement when session management feature is added
        return 0;
    }

    /**
     * User data validation
     */
    public void validateUserData(User user) {
        log.debug("User data validation started: userId={}", user.getId());
        
        validateEmail(user.getEmail());
        validatePhoneNumber(user.getPhoneNumber());
        validateUserProfile(user);
        
        log.debug("User data validation completed: userId={}", user.getId());
    }

    /**
     * Cleanup for logical deletion
     */
    private void performSoftDeleteCleanup(UUID userId, String reason) {
        log.info("Executing logical deletion cleanup: userId={}, reason={}", userId, reason);
        
        // Keep user preferences (for recovery purposes)
        // Keep activities (for audit purposes)
        
        // Clear cache and sessions
        clearUserCache(userId);
        deleteUserSessions(userId);
    }

    /**
     * Cleanup for physical deletion
     */
    private void performHardDeleteCleanup(UUID userId, String reason) {
        log.info("Executing physical deletion cleanup: userId={}, reason={}", userId, reason);
        
        // Delete all related data
        deleteUserPreferences(userId);
        deleteUserActivities(userId);
        deleteUserSessions(userId);
        clearUserCache(userId);
    }

    /**
     * Cleanup for event-driven deletion
     */
    private void performEventDrivenCleanup(UUID userId, String reason) {
        log.info("Executing event-driven deletion cleanup: userId={}, reason={}", userId, reason);
        
        // Processing for deletion events from authentication service
        deleteUserPreferences(userId);
        deleteUserActivities(userId);
        deleteUserSessions(userId);
        clearUserCache(userId);
    }

    /**
     * Clear user cache
     */
    private void clearUserCache(UUID userId) {
        log.debug("Clearing user cache: userId={}", userId);
        // TODO: Implement cache clearing process when Redis or other cache is available
    }

    /**
     * Email format validation
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    /**
     * Phone number format validation
     */
    private void validatePhoneNumber(String phoneNumber) {
        // Simple check for Japanese phone number format
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()
                && !phoneNumber.matches("^(\\+81|0)\\d{1,4}-?\\d{1,4}-?\\d{3,4}$")) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
    }

    /**
     * User profile validation
     */
    private void validateUserProfile(User user) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        
        if (user.getStatus() == null) {
            throw new IllegalArgumentException("User status is required");
        }
    }

    /**
     * Cleanup type
     */
    public enum CleanupType {
        SOFT_DELETE,    // For logical deletion
        HARD_DELETE,    // For physical deletion
        EVENT_DRIVEN    // For event-driven deletion
    }
}
