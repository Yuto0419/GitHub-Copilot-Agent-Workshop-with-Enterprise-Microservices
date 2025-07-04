package com.skishop.payment.dto;

/**
 * Address request
 *
 * @param line1 Address line 1
 * @param line2 Address line 2
 * @param city City
 * @param state State/Prefecture
 * @param postalCode Postal code
 * @param country Country
 */
public record AddressRequest(
    String line1,
    String line2,
    String city,
    String state,
    String postalCode,
    String country
) {}
