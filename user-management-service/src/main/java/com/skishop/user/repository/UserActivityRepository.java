package com.skishop.user.repository;

import com.skishop.user.entity.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Activity Repository
 */
@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {

    /**
     * Find activities by user ID (sorted by creation date in descending order)
     */
    Page<UserActivity> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find by user ID and activity type
     */
    List<UserActivity> findByUser_IdAndActivityType(UUID userId, UserActivity.ActivityType activityType);

    /**
     * Find by activity type
     */
    Page<UserActivity> findByActivityType(UserActivity.ActivityType activityType, Pageable pageable);

    /**
     * Search for activities within a specified period
     */
    @Query("SELECT a FROM UserActivity a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<UserActivity> findActivitiesBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get the user's latest login time
     */
    @Query("SELECT MAX(a.createdAt) FROM UserActivity a WHERE a.user.id = :userId AND a.activityType = 'LOGIN'")
    Optional<LocalDateTime> findLatestLoginByUserId(@Param("userId") UUID userId);

    /**
     * Search for security-related activities
     */
    @Query("""
           SELECT a FROM UserActivity a WHERE a.user.id = :userId 
           AND a.activityType IN ('LOGIN', 'LOGOUT', 'PASSWORD_CHANGE', 'EMAIL_VERIFY', 'PHONE_VERIFY') 
           ORDER BY a.createdAt DESC
           """)
    List<UserActivity> findSecurityActivitiesByUserId(@Param("userId") UUID userId);

    /**
     * Activity statistics by IP address
     */
    @Query("SELECT a.ipAddress, COUNT(a) FROM UserActivity a WHERE a.user.id = :userId GROUP BY a.ipAddress")
    List<Object[]> countActivitiesByIpAddress(@Param("userId") UUID userId);

    /**
     * Statistics by activity type
     */
    @Query("SELECT a.activityType, COUNT(a) FROM UserActivity a WHERE a.createdAt >= :since GROUP BY a.activityType")
    List<Object[]> countActivitiesByTypeSince(@Param("since") LocalDateTime since);

    /**
     * Search for activities by user entity
     */
    List<UserActivity> findByUser(com.skishop.user.entity.User user);

    /**
     * Paginated search for activities by user entity
     */
    Page<UserActivity> findByUser(com.skishop.user.entity.User user, Pageable pageable);

    /**
     * Search for activities by user entity ordered by date
     */
    List<UserActivity> findByUserOrderByTimestampDesc(com.skishop.user.entity.User user);

    /**
     * Delete activities by user entity
     */
    void deleteByUser(com.skishop.user.entity.User user);

    /**
     * Delete activities by user ID
     */
    int deleteByUserId(UUID userId);

    /**
     * Find activities by activity type (string)
     */
    @Query("SELECT ua FROM UserActivity ua WHERE ua.activityType = :activityType")
    List<UserActivity> findByActivityTypeString(@Param("activityType") String activityType);
}
