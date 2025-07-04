package com.skishop.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Role entity
 * Manages user permissions
 */
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Predefined roles
     */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_STORE_MANAGER = "STORE_MANAGER";
    public static final String ROLE_SUPPORT = "SUPPORT";

    /**
     * Check if role has the specified permission
     */
    public boolean hasPermission(String permissionName) {
        return permissions != null && 
               permissions.stream()
                         .anyMatch(permission -> permissionName.equals(permission.getName()));
    }

    /**
     * Check if role is admin
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(name);
    }

    /**
     * Check if role is customer
     */
    public boolean isCustomer() {
        return ROLE_CUSTOMER.equals(name);
    }
}
