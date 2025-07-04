package com.skishop.sales.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Return Application Request DTO
 */
@Data
public class ReturnCreateRequest {

    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Order item ID is required")
    private String orderItemId;

    @NotBlank(message = "Return reason is required")
    @Pattern(regexp = "DEFECTIVE|WRONG_ITEM|SIZE_ISSUE|NOT_AS_DESCRIBED|DAMAGED_SHIPPING|CUSTOMER_CHANGED_MIND|OTHER", 
             message = "Please specify a valid return reason")
    private String reason;

    @Size(max = 1000, message = "Return reason details must be within 1000 characters")
    private String reasonDetail;

    @NotNull(message = "Return quantity is required")
    @Min(value = 1, message = "Return quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.00", message = "Refund amount must be 0 or greater")
    private BigDecimal refundAmount;
}
