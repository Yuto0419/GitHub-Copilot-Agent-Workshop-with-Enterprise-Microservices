package com.skishop.user.repository;

import com.skishop.user.entity.User;
import com.skishop.user.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User preference repository
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    /**
     * Find preferences by user ID
     */
    List<UserPreference> findByUser_Id(UUID userId);

    /**
     * Find preference by user ID and preference key
     */
    Optional<UserPreference> findByUser_IdAndPrefKey(UUID userId, String prefKey);

    /**
     * Find preferences by preference key
     */
    List<UserPreference> findByPrefKey(String prefKey);

    /**
     * Check existence by user ID and preference key
     */
    boolean existsByUser_IdAndPrefKey(UUID userId, String prefKey);

    /**
     * Delete by user ID and preference key
     */
    void deleteByUser_IdAndPrefKey(UUID userId, String prefKey);

    /**
     * Delete all preferences by user ID
     */
    void deleteByUser_Id(UUID userId);

    /**
     * Find preferences by user entity
     */
    Optional<UserPreference> findByUser(User user);

    /**
     * Delete preferences by user entity
     */
    void deleteByUser(User user);

    /**
     * Check existence by user entity
     */
    boolean existsByUser(User user);

    /**
     * Delete preferences by user ID
     */
    int deleteByUserId(UUID userId);
}
