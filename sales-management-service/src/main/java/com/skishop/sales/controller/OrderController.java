package com.skishop.sales.controller;

import com.skishop.sales.dto.request.OrderCreateRequest;
import com.skishop.sales.dto.request.OrderStatusUpdateRequest;
import com.skishop.sales.dto.response.OrderResponse;
import com.skishop.sales.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Order Controller
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "Order Management API")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create Order
     */
    @PostMapping
    @Operation(summary = "Create Order", description = "Creates a new order")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        log.info("Creating order for customer: {}", request.customerId());
        
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Get order by the specified ID")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId) {
        log.info("Getting order: {}", orderId);
        
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Get order using the order number")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @Parameter(description = "Order number") @PathVariable String orderNumber) {
        log.info("Getting order by number: {}", orderNumber);
        
        OrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get customer orders
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Get orders for the specified customer")
    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomer(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting orders for customer: {}", customerId);
        
        Page<OrderResponse> response = orderService.getOrdersByCustomer(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        log.info("Updating order status: {} to {}", orderId, request.status());
        
        OrderResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        OrderResponse response = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Search orders
     * Uses Java 21 Switch expressions to handle search criteria
     */
    @GetMapping("/search")
    @Operation(summary = "Search orders", description = "Search orders with specified criteria")
    public ResponseEntity<Page<OrderResponse>> searchOrders(
            @Parameter(description = "Customer ID") @RequestParam(required = false) String customerId,
            @Parameter(description = "Order status") @RequestParam(required = false) String status,
            @Parameter(description = "Payment status") @RequestParam(required = false) String paymentStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        
        var searchCriteria = String.format("customerId=%s, status=%s, paymentStatus=%s", 
                customerId, status, paymentStatus);
        log.info("Searching orders with criteria: {}", searchCriteria);
        
        // Search logic using Java 21 Switch expressions (for future implementation)
        var searchType = switch (customerId != null ? "HAS_CUSTOMER" : "NO_CUSTOMER") {
            case "HAS_CUSTOMER" -> status != null ? "CUSTOMER_AND_STATUS" : "CUSTOMER_ONLY";
            case "NO_CUSTOMER" -> status != null ? "STATUS_ONLY" : "ALL";
            default -> "ALL";
        };
        
        log.debug("Search type determined: {}", searchType);
        
        // Search logic based on search conditions (currently returns empty results)
        // Future implementation will include the following search types:
        // - CUSTOMER_AND_STATUS: Filter by customer ID and status
        // - CUSTOMER_ONLY: Filter by customer ID
        // - STATUS_ONLY: Filter by status
        // - ALL: Get all
        Page<OrderResponse> response = Page.empty();
        return ResponseEntity.ok(response);
    }
}
