package com.skishop.user.repository;

import com.skishop.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository
 * Provides access to user data
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find users by status
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * Find active users
     */
    List<User> findByStatusAndEmailVerified(User.UserStatus status, Boolean emailVerified);

    /**
     * Search by partial name match
     */
    @Query("""
           SELECT u FROM User u WHERE 
           LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR 
           LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
           """)
    Page<User> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find users created within date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get count of users created since specified date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    Long countUsersCreatedAfter(@Param("date") LocalDateTime date);

    /**
     * Get user count by role
     */
    @Query("SELECT u.role.name, COUNT(u) FROM User u GROUP BY u.role.name")
    List<Object[]> countUsersByRole();

    /**
     * Find unverified email users
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recently logged in active users
     */
    @Query("""
           SELECT DISTINCT u FROM User u 
           JOIN u.activities a 
           WHERE u.status = :status 
           AND a.activityType = 'LOGIN' 
           AND a.createdAt >= :since
           """)
    List<User> findActiveUsersWithRecentLogin(
        @Param("status") User.UserStatus status,
        @Param("since") LocalDateTime since
    );

    /**
     * Find users by age range
     */
    @Query("""
           SELECT u FROM User u WHERE 
           EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM u.birthDate) BETWEEN :minAge AND :maxAge
           AND u.birthDate IS NOT NULL
           """)
    List<User> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

    /**
     * Find users by gender and status
     */
    List<User> findByGenderAndStatus(User.Gender gender, User.UserStatus status);

    /**
     * Find users by multiple IDs
     */
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIds(@Param("ids") List<UUID> ids);

    /**
     * Find user by user ID (not needed - ID is UUID)
     */
    // Optional<User> findByUserId(String userId);

    /**
     * Check if user ID exists (not needed - ID is UUID)
     */
    // boolean existsByUserId(String userId);

    /**
     * Get user count by status
     */
    long countByStatus(User.UserStatus status);

    /**
     * Get count of users created after specified date
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Search with multiple criteria
     */
    Page<User> findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String email, String firstName, String lastName, Pageable pageable);
}
