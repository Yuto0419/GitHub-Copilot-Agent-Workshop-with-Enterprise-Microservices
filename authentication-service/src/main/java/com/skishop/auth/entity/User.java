package com.skishop.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User Entity
 *
 * Entity for managing user information in the authentication service
 */
@Entity
@Table(name = "users")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;
    
    @Column(unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Column(name = "status", length = 50)
    private String status; // PENDING_VERIFICATION, ACTIVE, SUSPENDED, etc.

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login")
    private Instant lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relations
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserSession> sessions = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserMFA mfa;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PasswordReset> passwordResets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SecurityLog> securityLogs = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();

    /**
     * Get username
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Get status
     */
    public String getStatus() {
        return this.status;
    }
    
    /**
     * Set status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Check if email is verified
     */
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }

    /**
     * Check if the user is locked
     */
    public boolean isLocked() {
        return accountLocked != null && accountLocked;
    }

    /**
     * Check if the user is active
     */
    public boolean isActive() {
        return isActive != null && isActive;
    }

    /**
     * Check if the user is active (for compatibility with existing methods)
     */
    public boolean isAccountActive() {
        return isActive != null && isActive;
    }

    /**
     * Check if MFA is enabled
     */
    public boolean isMfaEnabled() {
        return mfa != null && mfa.getIsEnabled();
    }

    /**
     * Lock the account
     */
    public void setLocked(boolean locked) {
        this.accountLocked = locked;
        if (locked) {
            this.lockedAt = Instant.now();
        }
    }

    /**
     * Set the lock time
     */
    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    /**
     * Update the last login time
     */
    public void updateLastLogin() {
        this.lastLogin = Instant.now();
    }
}
