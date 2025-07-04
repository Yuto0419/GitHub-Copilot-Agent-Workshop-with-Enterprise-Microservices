package com.skishop.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * System statistics response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsResponse {
    
    /**
     * Total number of users
     */
    private Long totalUsers;
    
    /**
     * Number of active users
     */
    private Long activeUsers;
    
    /**
     * Number of pending users
     */
    private Long pendingUsers;
    
    /**
     * Number of inactive users
     */
    private Long inactiveUsers;
    
    /**
     * Total number of roles
     */
    private Long totalRoles;
    
    /**
     * Number of recent registrations
     */
    private Long recentRegistrations;
}
