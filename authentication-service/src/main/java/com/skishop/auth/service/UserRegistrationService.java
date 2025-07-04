package com.skishop.auth.service;

import com.skishop.auth.dto.request.UserCreateRequest;
import com.skishop.auth.dto.response.UserResponse;
import com.skishop.auth.entity.User;
import com.skishop.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User registration service.
 * Manages user registration and deletion, and publishes events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublishingService eventPublishingService;

    /**
     * User registration
     */
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        // Check for duplicate email address
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Create user entity
        User user = User.builder()
            .id(UUID.randomUUID())
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .status("PENDING")
            .emailVerified(false)
            .build();

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("User saved with ID: {}", savedUser.getId());

        try {
            // Publish user registration event
            eventPublishingService.publishUserRegisteredEvent(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                null // phoneNumber - not available in the current registration request
            );
            log.info("User registration event published for user: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to publish user registration event for user {}: {}", savedUser.getId(), e.getMessage());
            // Even if event publishing fails, user registration is considered successful (handled by compensation transaction)
        }

        // Create response
        return UserResponse.builder()
            .id(savedUser.getId().toString())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .status(savedUser.getStatus())
            .emailVerified(savedUser.isEmailVerified())
            .createdAt(savedUser.getCreatedAt() != null ? 
                LocalDateTime.ofInstant(savedUser.getCreatedAt(), java.time.ZoneOffset.UTC) : null)
            .updatedAt(savedUser.getUpdatedAt() != null ? 
                LocalDateTime.ofInstant(savedUser.getUpdatedAt(), java.time.ZoneOffset.UTC) : null)
            .build();
    }

    /**
     * User deletion
     */
    @Transactional
    public void deleteUser(UUID userId, String reason) {
        log.info("Deleting user: {} with reason: {}", userId, reason);

        // Check if user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        try {
            // Publish user deletion event first
            eventPublishingService.publishUserDeletedEvent(userId, reason);
            log.info("User deletion event published for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish user deletion event for user {}: {}", userId, e.getMessage());
            // If event publishing fails, abort deletion process
            throw new RuntimeException("Failed to publish user deletion event", e);
        }

        // Logically delete user (change status instead of physical deletion)
        user.setStatus("DELETED");
        userRepository.save(user);
        
        log.info("User {} marked as deleted", userId);
    }

    /**
     * Physical user deletion (admin only)
     */
    @Transactional
    public void hardDeleteUser(UUID userId, String reason) {
        log.info("Hard deleting user: {} with reason: {}", userId, reason);

        // Check if user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        try {
            // Publish user deletion event first
            eventPublishingService.publishUserDeletedEvent(userId, "HARD_DELETE: " + reason);
            log.info("User hard deletion event published for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish user hard deletion event for user {}: {}", userId, e.getMessage());
            // If event publishing fails, abort deletion process
            throw new RuntimeException("Failed to publish user deletion event", e);
        }

        // Physically delete user
        userRepository.delete(user);
        log.info("User {} physically deleted", userId);
    }
}
