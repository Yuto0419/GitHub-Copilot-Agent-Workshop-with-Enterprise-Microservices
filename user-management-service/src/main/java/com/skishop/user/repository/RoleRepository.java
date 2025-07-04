package com.skishop.user.repository;

import com.skishop.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Role repository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find by role name
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role name exists
     */
    boolean existsByName(String name);

    /**
     * Find roles with the specified permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Get all roles ordered by name
     */
    List<Role> findAllByOrderByName();
}
