package com.skishop.auth.repository;

import com.skishop.auth.entity.User;
import com.skishop.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Session Repository
 * 
 * Data access layer for user session entities
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /**
     * Find session by session token
     */
    Optional<UserSession> findBySessionToken(String sessionToken);

    /**
     * Find session by refresh token
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * Get list of active sessions for user
     */
    @Query("SELECT s FROM UserSession s WHERE s.user = :user AND s.isActive = true AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUser(@Param("user") User user, @Param("now") Instant now);

    /**
     * Get all sessions for user
     */
    List<UserSession> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Delete all sessions by user ID
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Deactivate expired sessions
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt < :now")
    int deactivateExpiredSessions(@Param("now") Instant now);

    /**
     * Deactivate all sessions for user
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user = :user")
    int deactivateAllUserSessions(@Param("user") User user);

    /**
     * Deactivate all user sessions except the specified session
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user = :user AND s.id != :sessionId")
    int deactivateOtherUserSessions(@Param("user") User user, @Param("sessionId") UUID sessionId);

    /**
     * Delete expired sessions
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :cutoffDate")
    int deleteExpiredSessions(@Param("cutoffDate") Instant cutoffDate);
}
