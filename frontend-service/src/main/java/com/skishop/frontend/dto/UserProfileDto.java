package com.skishop.frontend.dto;

/**
 * User profile DTO
 */
public record UserProfileDto(
    String id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    AddressDto defaultAddress,
    PreferencesDto preferences
) {}
