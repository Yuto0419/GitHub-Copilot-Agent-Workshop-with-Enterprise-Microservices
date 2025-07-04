package com.skishop.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Password Reset Entity
 *
 * Entity for managing password reset requests
 */
@Entity
@Table(name = "password_resets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reset_token", unique = true, nullable = false, length = 255)
    private String resetToken;

    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Check if the token is valid
     */
    public boolean isValid() {
        return Boolean.FALSE.equals(isUsed) && expiresAt.isAfter(Instant.now());
    }

    /**
     * Mark the token as used
     */
    public void markAsUsed() {
        this.isUsed = true;
    }
}
