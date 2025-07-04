package com.skishop.auth.repository;

import com.skishop.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository
 * 
 * Data access layer for user entities
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email address exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Find active user by email address
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * Find unlocked user by email address
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.accountLocked = false")
    Optional<User> findUnlockedUserByEmail(@Param("email") String email);

    /**
     * Get number of users created within the specified period
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersCreatedSince(@Param("startDate") Instant startDate);

    /**
     * Get list of locked users
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true")
    List<User> findLockedUsers();

    /**
     * Get list of users with unverified email
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findUnverifiedUsers();
}
