package com.skishop.sales.controller;

import com.skishop.sales.dto.request.ReturnCreateRequest;
import com.skishop.sales.dto.request.ReturnStatusUpdateRequest;
import com.skishop.sales.dto.response.ReturnDetailResponse;
import com.skishop.sales.dto.response.ReturnListResponse;
import com.skishop.sales.dto.response.ReturnResponse;
import com.skishop.sales.service.ReturnService;
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
 * Return Management API Controller
 */
@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Return API", description = "Return Management API")
public class ReturnController {

    private final ReturnService returnService;

    /**
     * Get returns list
     */
    @GetMapping
    @Operation(summary = "Get Returns List", description = "Retrieves a list of return information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved returns list"),
        @ApiResponse(responseCode = "403", description = "You do not have permission to access this resource")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE') or hasRole('RETURN_PROCESSOR')")
    public ResponseEntity<ReturnListResponse> getReturns(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Return status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Start date") @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date") @RequestParam(required = false) String toDate) {
        
        log.info("Getting returns with status: {}, page: {}", status, pageable);
        ReturnListResponse response = returnService.getReturns(pageable, status, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get return details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Return Details", description = "Retrieves detailed information for the specified return ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved return details"),
        @ApiResponse(responseCode = "404", description = "Return information not found"),
        @ApiResponse(responseCode = "403", description = "You do not have permission to access this resource")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE') or hasRole('RETURN_PROCESSOR')")
    public ResponseEntity<ReturnDetailResponse> getReturnDetail(
            @Parameter(description = "Return ID") @PathVariable Long id) {
        
        log.info("Getting return detail for id: {}", id);
        ReturnDetailResponse response = returnService.getReturnDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create return request
     */
    @PostMapping
    @Operation(summary = "Create Return Request", description = "Creates a new return request")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Successfully created return request"),
        @ApiResponse(responseCode = "400", description = "Invalid request content"),
        @ApiResponse(responseCode = "403", description = "You do not have permission to access this resource")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE') or hasRole('USER')")
    public ResponseEntity<ReturnDetailResponse> createReturn(
            @Parameter(description = "Return creation request") @Valid @RequestBody ReturnCreateRequest request) {
        
        log.info("Creating return for order: {}", request.getOrderId());
        ReturnDetailResponse response = returnService.createReturn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update return status
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update Return Status", description = "Updates the status of a return")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully updated status"),
        @ApiResponse(responseCode = "404", description = "Return information not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request content"),
        @ApiResponse(responseCode = "403", description = "You do not have permission to access this resource")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE') or hasRole('RETURN_PROCESSOR')")
    public ResponseEntity<ReturnResponse> updateReturnStatus(
            @Parameter(description = "Return ID") @PathVariable Long id,
            @Parameter(description = "Status update request") @Valid @RequestBody ReturnStatusUpdateRequest request) {
        
        log.info("Updating return status for id: {}, status: {}", id, request.status());
        ReturnResponse response = returnService.updateReturnStatus(id.toString(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get order return information
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get Order Return Information", description = "Retrieves return information for the specified order ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved return information"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "403", description = "You do not have permission to access this resource")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE') or hasRole('RETURN_PROCESSOR')")
    public ResponseEntity<ReturnListResponse> getReturnsByOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        log.info("Getting returns for order: {}", orderId);
        ReturnListResponse response = returnService.getReturnsByOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
