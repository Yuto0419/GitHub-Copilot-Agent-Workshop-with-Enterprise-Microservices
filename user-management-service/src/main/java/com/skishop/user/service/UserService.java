package com.skishop.user.service;

import com.skishop.user.dto.request.*;
import com.skishop.user.dto.response.*;
import com.skishop.user.entity.User;
import com.skishop.user.exception.UserNotFoundException;
import com.skishop.user.mapper.UserMapper;
import com.skishop.user.repository.UserRepository;
import com.skishop.user.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User service (basic CRUD only)
 * Provides basic user management functionality only
 * Event-driven processing is separated into UserEventService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MetricsService metricsService;
    private final UserQueryService userQueryService;
    private final UserDataService userDataService;

    /**
     * Get user list (basic search)
     */
    @Transactional(readOnly = true)
    public UserListResponse getUsers(Pageable pageable, String search, String status) {
        log.info("Getting users with search: {}, status: {}", search, status);
        
        UserQueryService.UserSearchCriteria criteria = new UserQueryService.UserSearchCriteria();
        criteria.setSearch(search);
        criteria.setStatus(status);
        
        Page<User> userPage = userQueryService.searchUsers(criteria, pageable);
        
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(userMapper::toResponse)
                .toList();
        
        return UserListResponse.builder()
                .users(userResponses)
                .totalCount((int) userPage.getTotalElements())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
    }

    /**
     * Get user details
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.info("Getting user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        return userMapper.toResponse(user);
    }

    /**
     * Create user
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.email());
        
        // Check for duplicate email addresses
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email '" + request.email() + "' already exists");
        }
        
        // Check for duplicate phone number (if provided)
        if (request.phoneNumber() != null && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("User with phone number '" + request.phoneNumber() + "' already exists");
        }
        
        User user = userMapper.toEntity(request);
        user.setStatus(User.UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user = userRepository.save(user);
        
        // Update metrics
        metricsService.incrementUserRegistrations();
        return userMapper.toResponse(user);
    }

    /**
     * Update user
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        // Check for duplicate email addresses (if changed)
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email '" + request.email() + "' already exists");
        }
        
        // Check for duplicate phone number (if changed)
        if (request.phoneNumber() != null && 
            !request.phoneNumber().equals(user.getPhoneNumber()) &&
            userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("User with phone number '" + request.phoneNumber() + "' already exists");
        }
        
        userMapper.updateEntity(user, request);
        user.setUpdatedAt(LocalDateTime.now());
        
        user = userRepository.save(user);
        
        return userMapper.toResponse(user);
    }

    /**
     * Delete user (logical deletion)
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        // Soft delete: change status to deleted
        user.setStatus(User.UserStatus.DELETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Clean up related data (delegated to UserDataService)
        userDataService.cleanupUserData(userId, UserDataService.CleanupType.SOFT_DELETE, "USER_REQUESTED");
        
        log.info("Successfully marked user as deleted: {}", userId);
    }

    /**
     * Check email availability
     */
    @Transactional(readOnly = true)
    public CheckEmailResponse checkEmailAvailability(String email) {
        log.info("Checking email availability: {}", email);
        
        boolean exists = userRepository.existsByEmail(email);
        String message = exists ? "Email is already taken" : "Email is available";
        
        return new CheckEmailResponse(email, exists, message);
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        log.info("Resending verification email to: {}", request.email());
        
        // Search for user by email address
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.email()));
        
        // Error if already verified
        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified for user: " + request.email());
        }
        
        // TODO: Implement email sending functionality
        log.info("Verification email resent to: {}", request.email());
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", username);
        
        // Verify user exists
        if (!userRepository.existsByEmail(username)) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        
        // In a real implementation, you would:
        // 1. Verify current password
        // 2. Hash new password
        // 3. Update user entity
        // For now, we'll just log the operation
        log.info("Password change requested for user: {} with new password length: {}", 
                username, request.newPassword() != null ? request.newPassword().length() : 0);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public CheckEmailResponse checkEmailExists(String email) {
        log.info("Checking if email exists: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        String message = exists ? "Email already exists" : "Email does not exist";
        return new CheckEmailResponse(email, exists, message);
    }

    /**
     * Check existence by user ID
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Search by user ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUserId(String userId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            return userRepository.findById(userUUID);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * getCurrentUser (for backward compatibility with other services)
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        log.info("Getting current user: {}", username);
        
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        return userMapper.toResponse(user);
    }
}