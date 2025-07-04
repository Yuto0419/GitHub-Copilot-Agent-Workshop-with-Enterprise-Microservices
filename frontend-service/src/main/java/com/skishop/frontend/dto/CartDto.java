package com.skishop.frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cart DTO (compatible with payment-cart-service)
 */
public record CartDto(
    @JsonProperty("id")
    String id,
    
    @JsonProperty("userId")
    String userId,
    
    @JsonProperty("items")
    List<CartItemDto> items,
    
    @JsonProperty("totalAmount")
    int totalAmount, // Returned as int type by payment-cart-service
    
    @JsonProperty("currency")
    String currency,
    
    @JsonProperty("itemCount")
    int itemCount
) {
    // Methods for compatibility with existing templates
    public BigDecimal subtotal() {
        return BigDecimal.valueOf(totalAmount);
    }
    
    public BigDecimal tax() {
        // Calculate with 10% consumption tax
        return BigDecimal.valueOf(totalAmount * 0.1);
    }
    
    public BigDecimal shipping() {
        // Shipping is free for orders over 5000 yen
        return totalAmount >= 5000 ? BigDecimal.ZERO : BigDecimal.valueOf(500);
    }
    
    public BigDecimal total() {
        return subtotal().add(tax()).add(shipping());
    }
}
