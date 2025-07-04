package com.skishop.user.service;

import com.skishop.user.dto.request.UserPreferenceUpdateRequest;
import com.skishop.user.dto.response.UserPreferenceResponse;
import com.skishop.user.dto.response.UserPreferencesListResponse;
import com.skishop.user.entity.User;
import com.skishop.user.entity.UserPreference;
import com.skishop.user.exception.UserNotFoundException;
import com.skishop.user.mapper.UserPreferenceMapper;
import com.skishop.user.repository.UserPreferenceRepository;
import com.skishop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User Preferences Management Service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserPreferenceMapper userPreferenceMapper;

    /**
     * Get list of user preferences
     */
    @Transactional(readOnly = true)
    public UserPreferencesListResponse getUserPreferences(String userId, Pageable pageable) {
        log.info("Getting user preferences for user: {}", userId);
        
        UUID userUuid = UUID.fromString(userId);
        List<UserPreference> preferences = userPreferenceRepository.findByUser_Id(userUuid);
        
        List<UserPreferenceResponse> preferenceResponses = preferences.stream()
                .map(userPreferenceMapper::toResponse)
                .toList();
        
        // Split results according to Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), preferenceResponses.size());
        List<UserPreferenceResponse> pagedResults = start >= preferenceResponses.size() ? 
            List.of() : preferenceResponses.subList(start, end);
        
        return UserPreferencesListResponse.builder()
                .preferences(pagedResults)
                .totalCount(preferences.size())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
    }

    /**
     * Get a user preference
     */
    @Transactional(readOnly = true)
    public UserPreferenceResponse getUserPreference(String userId, String key) {
        log.info("Getting user preference: userId={}, key={}", userId, key);
        
        UUID userUuid = UUID.fromString(userId);
        UserPreference preference = userPreferenceRepository.findByUser_IdAndPrefKey(userUuid, key)
                .orElseThrow(() -> new IllegalArgumentException("User preference not found: userId=" + userId + ", key=" + key));
        
        return userPreferenceMapper.toResponse(preference);
    }

    /**
     * Update a user preference
     */
    @Transactional
    public UserPreferenceResponse updateUserPreference(String userId, String key, UserPreferenceUpdateRequest request) {
        log.info("Updating user preference: userId={}, key={}", userId, key);
        
        UUID userUuid = UUID.fromString(userId);
        
        // Check if user exists
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        // Get existing preference or create new
        UserPreference preference = userPreferenceRepository.findByUser_IdAndPrefKey(userUuid, key)
                .orElse(UserPreference.builder()
                        .user(user)
                        .prefKey(key)
                        .prefType(UserPreference.PreferenceType.STRING) // Default type
                        .build());
        
        userPreferenceMapper.updateEntity(preference, request);
        preference.setUpdatedAt(LocalDateTime.now());
        
        preference = userPreferenceRepository.save(preference);
        
        return userPreferenceMapper.toResponse(preference);
    }

    /**
     * Delete a user preference
     */
    @Transactional
    public void deleteUserPreference(UUID userId, String key) {
        log.info("Deleting user preference: userId={}, key={}", userId, key);
        
        // Check if preference exists
        if (!userPreferenceRepository.existsByUser_IdAndPrefKey(userId, key)) {
            throw new IllegalArgumentException("User preference not found: userId=" + userId + ", key=" + key);
        }
        
        userPreferenceRepository.deleteByUser_IdAndPrefKey(userId, key);
        
        log.info("Successfully deleted user preference: userId={}, key={}", userId, key);
    }
}
