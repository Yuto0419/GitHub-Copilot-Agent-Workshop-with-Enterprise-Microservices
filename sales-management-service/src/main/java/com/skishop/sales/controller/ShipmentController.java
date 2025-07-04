package com.skishop.sales.controller;

import com.skishop.sales.dto.request.ShipmentCreateRequest;
import com.skishop.sales.dto.request.ShipmentStatusUpdateRequest;
import com.skishop.sales.dto.request.TrackingUpdateRequest;
import com.skishop.sales.dto.response.ShipmentDetailResponse;
import com.skishop.sales.dto.response.ShipmentListResponse;
import com.skishop.sales.dto.response.ShipmentResponse;
import com.skishop.sales.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Shipment Management API Controller
 */
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipment API", description = "Shipment Management API")
public class ShipmentController {

    private final ShipmentService shipmentService;

    /**
     * Get shipments list
     */
    @GetMapping
    @Operation(summary = "Get Shipments List", description = "Retrieves a list of shipment information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved shipments list"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS_MANAGER')")
    public ResponseEntity<ShipmentListResponse> getShipments(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Shipment status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Start date") @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date") @RequestParam(required = false) String toDate) {
        
        log.info("Getting shipments with status: {}, page: {}", status, pageable);
        ShipmentListResponse response = shipmentService.getShipments(pageable, status, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get shipment details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Shipment Details", description = "Retrieves detailed information for the specified shipment ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved shipment details"),
        @ApiResponse(responseCode = "404", description = "Shipment information not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS_MANAGER') or hasRole('CUSTOMER_SERVICE')")
    public ResponseEntity<ShipmentDetailResponse> getShipmentDetail(
            @Parameter(description = "Shipment ID") @PathVariable Long id) {
        
        log.info("Getting shipment detail for id: {}", id);
        ShipmentDetailResponse response = shipmentService.getShipmentDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create shipment
     */
    @PostMapping
    @Operation(summary = "Create Shipment", description = "Creates a new shipment record")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Shipment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request content"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS_MANAGER')")
    public ResponseEntity<ShipmentDetailResponse> createShipment(
            @Parameter(description = "Shipment creation request") @Valid @RequestBody ShipmentCreateRequest request) {
        
        log.info("Creating shipment for order: {}", request.getOrderId());
        ShipmentDetailResponse response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update shipment status
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update Shipment Status", description = "Updates the status of a shipment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Shipment information not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request content"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS_MANAGER')")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @Parameter(description = "Shipment ID") @PathVariable Long id,
            @Parameter(description = "Status update request") @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        
        log.info("Updating shipment status for id: {}, status: {}", id, request.status());
        ShipmentResponse response = shipmentService.updateShipmentStatus(id.toString(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get order shipment information
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get Order Shipment Information", description = "Retrieves shipment information for the specified order ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved shipment information"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS_MANAGER') or hasRole('CUSTOMER_SERVICE')")
    public ResponseEntity<ShipmentListResponse> getShipmentsByOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        log.info("Getting shipments for order: {}", orderId);
        ShipmentListResponse response = shipmentService.getShipmentsByOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update tracking information
     */
    @PutMapping("/{id}/tracking")
    @Operation(summary = "Update Tracking Information", description = "Updates the tracking information for a shipment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tracking information updated successfully"),
        @ApiResponse(responseCode = "404", description = "Shipment information not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request content"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS_MANAGER')")
    public ResponseEntity<ShipmentResponse> updateTracking(
            @Parameter(description = "Shipment ID") @PathVariable Long id,
            @Parameter(description = "Tracking information update request") @Valid @RequestBody TrackingUpdateRequest request) {
        
        log.info("Updating tracking info for shipment: {}", id);
        ShipmentResponse response = shipmentService.updateTracking(id.toString(), request);
        return ResponseEntity.ok(response);
    }
}
