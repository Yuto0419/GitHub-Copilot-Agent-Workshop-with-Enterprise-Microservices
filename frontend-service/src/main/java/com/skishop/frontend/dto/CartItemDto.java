package com.skishop.frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Cart item DTO (compatible with payment-cart-service)
 */
public record CartItemDto(
    @JsonProperty("id")
    String id,
    
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("quantity")
    int quantity,
    
    @JsonProperty("unitPrice")
    int unitPrice, // Returned as int type by payment-cart-service
    
    @JsonProperty("totalPrice")
    int totalPrice, // Returned as int type by payment-cart-service
    
    @JsonProperty("productDetails")
    Map<String, Object> productDetails
) {
    // Method for compatibility with existing templates
    public String productName() {
        if (productDetails != null) {
            Object name = productDetails.get("name");
            return name != null ? name.toString() : "No product name";
        }
        return "No product name";
    }
    
    public String imageUrl() {
        if (productDetails != null) {
            Object imageUrl = productDetails.get("imageUrl");
            return imageUrl != null ? imageUrl.toString() : "/images/no-image.png";
        }
        return "/images/no-image.png";
    }
    
    // Getter methods for BigDecimal (for template compatibility)
    public BigDecimal getUnitPriceBigDecimal() {
        return BigDecimal.valueOf(unitPrice);
    }
    
    public BigDecimal getTotalPriceBigDecimal() {
        return BigDecimal.valueOf(totalPrice);
    }
}
