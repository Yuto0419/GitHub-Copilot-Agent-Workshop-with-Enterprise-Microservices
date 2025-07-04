package com.skishop.auth.repository;

import com.skishop.auth.entity.PasswordReset;
import com.skishop.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Password Reset Repository
 *
 * Data access layer for PasswordReset entity
 */
@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {

    /**
     * Find password reset request by reset token
     */
    Optional<PasswordReset> findByResetToken(String resetToken);

    /**
     * Find valid reset token
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.resetToken = :token AND pr.isUsed = false AND pr.expiresAt > :now")
    Optional<PasswordReset> findValidResetToken(@Param("token") String token, @Param("now") Instant now);

    /**
     * Find valid reset request for user
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user AND pr.isUsed = false AND pr.expiresAt > :now")
    Optional<PasswordReset> findValidResetByUser(@Param("user") User user, @Param("now") Instant now);

    /**
     * Delete expired reset requests
     */
    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.expiresAt < :cutoffDate")
    int deleteExpiredResets(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Invalidate all reset requests for a user
     */
    @Modifying
    @Query("UPDATE PasswordReset pr SET pr.isUsed = true WHERE pr.user = :user AND pr.isUsed = false")
    int invalidateUserResets(@Param("user") User user);
}
