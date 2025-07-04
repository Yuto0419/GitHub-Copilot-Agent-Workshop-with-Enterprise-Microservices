package com.skishop.frontend.dto;

/**
 * User preferences DTO
 */
public record PreferencesDto(
    String language,
    String currency,
    boolean emailNotifications,
    boolean smsNotifications
) {}
