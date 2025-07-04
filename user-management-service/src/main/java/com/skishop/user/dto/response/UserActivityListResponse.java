package com.skishop.user.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User activity list response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityListResponse {

    private List<UserActivityResponse> activities;
    private int totalCount;
    private int page;
    private int size;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityResponse {
        private String id;
        private String userId;
        private String activityType;
        private String description;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;
    }
}
