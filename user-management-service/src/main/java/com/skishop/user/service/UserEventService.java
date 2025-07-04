package com.skishop.user.service;

import com.skishop.user.dto.event.UserRegistrationPayload;
import com.skishop.user.dto.event.UserDeletionPayload;
import com.skishop.user.entity.User;
import com.skishop.user.entity.UserActivity;
import com.skishop.user.repository.UserRepository;
import com.skishop.user.repository.UserActivityRepository;
import com.skishop.user.repository.UserPreferenceRepository;
import com.skishop.user.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * User Event Driven Processing Service
 * User profile management based on events from the authentication service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventService {

    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final MetricsService metricsService;

    /**
     * User profile creation (event-driven)
     * Creates a profile based on user registration events from the authentication service
     */
    @Transactional
    public User createUserProfile(UserRegistrationPayload payload) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Starting user profile creation: id={}, email={}", payload.getUserId(), payload.getEmail());

            // Check for duplicates
            if (userRepository.existsById(UUID.fromString(payload.getUserId()))) {
                log.warn("User already exists: id={}", payload.getUserId());
                throw new IllegalArgumentException("User already exists: " + payload.getUserId());
            }

            // Create user entity
            User user = User.builder()
                    .id(UUID.fromString(payload.getUserId()))
                    .email(payload.getEmail())
                    .firstName(payload.getFirstName())
                    .lastName(payload.getLastName())
                    .phoneNumber(payload.getPhoneNumber())
                    .status(User.UserStatus.valueOf(payload.getStatus()))
                    .emailVerified(false)
                    .phoneVerified(false)
                    .createdAt(payload.getCreatedAt() != null ? 
                        LocalDateTime.ofInstant(payload.getCreatedAt(), java.time.ZoneId.systemDefault()) 
                        : LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            user = userRepository.save(user);

            // Record activity
            recordUserActivity(user.getId(), "PROFILE_CREATED", "User profile created from registration event");

            // Record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordUserRegistration(true, processingTime);
            
            log.info("User profile creation completed: id={}, processingTime={}ms", user.getId(), processingTime);

            return user;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("User profile creation failed: id={}, processingTime={}ms, error={}", 
                    payload.getUserId(), processingTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * User profile deletion (event-driven)
     * Deletes a profile based on user deletion events from the authentication service
     */
    @Transactional
    public void deleteUserProfile(UUID id) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Starting user profile deletion: id={}", id);

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                // For idempotency, treat as success if user doesn't exist
                log.info("User to delete not found (treated as success for idempotency): id={}", id);
                return;
            }

            User user = userOpt.get();

            // Delete related data
            deleteRelatedData(user);

            // Delete user
            userRepository.delete(user);

            // Record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordUserDeletion(true, processingTime);

            log.info("User profile deletion completed: id={}, processingTime={}ms", id, processingTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("User profile deletion failed: id={}, processingTime={}ms, error={}", 
                    id, processingTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handle user deletion event
     */
    @Transactional
    public void handleUserDeletionEvent(UserDeletionPayload payload) {
        log.info("Starting user deletion event processing: id={}, reason={}", payload.getUserId(), payload.getReason());
        
        UUID userId = UUID.fromString(payload.getUserId());
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                // For idempotency, consider it successful even if the user doesn't exist
                log.info("User to be deleted not found (treated as success for idempotency): id={}", userId);
                return;
            }

            User user = userOpt.get();

            // Delete related data
            deleteRelatedData(user);

            // Delete user
            userRepository.delete(user);

            // Record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordUserDeletion(true, processingTime);

            log.info("User deletion event processing completed: id={}, processingTime={}ms", payload.getUserId(), processingTime);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("User deletion event processing failed: id={}, processingTime={}ms, error={}", 
                    payload.getUserId(), processingTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Process user registration event from authentication service
     */
    @Transactional
    public void handleUserRegisteredFromAuth(UUID userId, String email, String firstName, String lastName, String phoneNumber) {
        log.info("Handling user registration event from auth service for user: {}", userId);

        // Check if user already exists
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            log.info("User {} already exists in user management service, updating if needed", userId);
            User user = existingUser.get();
            
            // Update information as needed
            boolean updated = false;
            if (!email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            if (!firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                updated = true;
            }
            if (!lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                updated = true;
            }
            
            if (updated) {
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Updated existing user {} in user management service", userId);
            }
        } else {
            // Create a new user in the user management service
            User newUser = User.builder()
                .id(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .status(User.UserStatus.ACTIVE) // Managed as ACTIVE in the user management service
                .emailVerified(false) // Managed by the authentication service
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            userRepository.save(newUser);
            log.info("Created new user {} in user management service", userId);
            
            // Additional user management service-specific processing
            initializeUserProfile(userId);
        }
    }

    /**
     * Process user deletion event from authentication service
     */
    @Transactional
    public void handleUserDeletedFromAuth(UUID userId, String reason) {
        log.info("Handling user deletion event from auth service for user: {} with reason: {}", userId, reason);

        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User existingUser = user.get();
            
            // Change user to deleted status
            existingUser.setStatus(User.UserStatus.DELETED);
            existingUser.setUpdatedAt(LocalDateTime.now());
            userRepository.save(existingUser);
            
            log.info("Marked user {} as deleted in user management service", userId);
            
            // Additional cleanup processing
            cleanupUserData(userId, reason);
        } else {
            log.warn("Received user deletion event for non-existent user: {}", userId);
        }
    }

    /**
     * Initialize user profile
     */
    private void initializeUserProfile(UUID userId) {
        try {
            // Create default user preferences
            createDefaultUserPreferences(userId);
            
            // Record activity
            recordUserActivity(userId, "PROFILE_INITIALIZED", "User profile initialized from auth service");
            
            log.info("User profile initialized: {}", userId);
        } catch (Exception e) {
            log.error("Failed to initialize user profile: {}, error: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Create default user preferences
     */
    private void createDefaultUserPreferences(UUID userId) {
        try {
            // Implement UserPreference creation logic here
            log.debug("Created default user preferences for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to create default user preferences: userId={}, error={}", 
                     userId, e.getMessage());
        }
    }

    /**
     * User data cleanup
     */
    private void cleanupUserData(UUID userId, String reason) {
        try {
            // Process for deleting related data
            deleteRelatedData(userRepository.findById(userId).orElse(null));
            
            // Record activity
            recordUserActivity(userId, "DATA_CLEANUP", "User data cleaned up due to: " + reason);
            
            log.info("User data cleanup completed: {}", userId);
        } catch (Exception e) {
            log.error("Failed to cleanup user data: {}, error: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Delete related data
     */
    private void deleteRelatedData(User user) {
        UUID userId = user.getId();
        
        log.info("Starting related data deletion: userId={}", userId);

        // Delete user preferences
        int deletedPreferences = userPreferenceRepository.deleteByUserId(userId);
        log.info("User preferences deletion completed: userId={}, count={}", userId, deletedPreferences);

        // Delete user activities
        int deletedActivities = userActivityRepository.deleteByUserId(userId);
        log.info("User activities deletion completed: userId={}, count={}", userId, deletedActivities);

        // Add other related data if any

        log.info("Related data deletion completed: userId={}", userId);
    }

    /**
     * Record user activity
     */
    private void recordUserActivity(UUID userId, String action, String description) {
        try {
            // Get user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            UserActivity activity = UserActivity.builder()
                    .user(user)
                    .activityType(UserActivity.ActivityType.PROFILE_UPDATE) // Use default type
                    .description(description)
                    .ipAddress("system")
                    .userAgent("event-driven")
                    .build();

            userActivityRepository.save(activity);
            log.debug("User activity recorded: userId={}, action={}", userId, action);
        } catch (Exception e) {
            log.error("Failed to record user activity: userId={}, action={}, error={}", 
                    userId, action, e.getMessage());
            // Activity recording failure does not affect the main process
        }
    }

    /**
     * Force delete user profile (for compensation processing)
     * Complete deletion of user profile used in Saga compensation processing
     */
    @Transactional
    public void hardDeleteUserProfile(UUID id) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Compensation process: Starting forced user profile deletion: id={}", id);

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                log.info("Compensation process: Target user not found (treated as success due to idempotency): id={}", id);
                return;
            }

            User user = userOpt.get();

            // Delete related data
            deleteRelatedData(user);

            // Complete user deletion
            userRepository.delete(user);

            // Record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            metricsService.recordUserDeletion(true, processingTime);
            metricsService.recordCompensationExecuted("USER_REGISTRATION", "HARD_DELETE", processingTime, true);

            log.info("Compensation process: User profile forced deletion completed: id={}, processingTime={}ms", id, processingTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Compensation process: User profile forced deletion failed: id={}, processingTime={}ms, error={}", 
                    id, processingTime, e.getMessage(), e);
            
            metricsService.recordCompensationExecuted("USER_REGISTRATION", "HARD_DELETE", processingTime, false);
            throw e;
        }
    }

    /**
     * Check existence by user ID
     */
    public boolean existsByUserId(String userId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            return userRepository.existsById(userUUID);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check existence by email address
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Find user by user ID
     */
    public Optional<User> findByUserId(String userId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            return userRepository.findById(userUUID);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
