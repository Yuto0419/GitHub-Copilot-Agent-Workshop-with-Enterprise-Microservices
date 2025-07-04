package com.skishop.frontend.dto;

import java.util.UUID;

/**
 * Add to cart request DTO
 */
public record AddToCartRequest(
    String productId,
    int quantity
) {
    // UUID conversion method required by payment-cart-service API
    public UUID getProductIdAsUUID() {
        try {
            return UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            // If not a UUID, generate a random UUID (for testing)
            return UUID.randomUUID();
        }
    }
}
