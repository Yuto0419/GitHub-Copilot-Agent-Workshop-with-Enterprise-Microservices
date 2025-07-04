package com.skishop.sales.controller;

import com.skishop.sales.dto.response.ProductSalesReportResponse;
import com.skishop.sales.dto.response.ReturnReportResponse;
import com.skishop.sales.dto.response.SalesReportResponse;
import com.skishop.sales.dto.response.ShippingReportResponse;
import com.skishop.sales.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Reports API Controller
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports API", description = "Reports and Analytics API")
public class ReportsController {

    private final ReportsService reportsService;

    /**
     * Get sales report
     */
    @GetMapping("/sales")
    @Operation(summary = "Get sales report", description = "Retrieves the sales report for the specified period")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales report"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALES_MANAGER') or hasRole('ANALYST')")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Grouping method (DAILY, WEEKLY, MONTHLY)") @RequestParam(defaultValue = "DAILY") String groupBy) {
        
        log.info("Getting sales report from {} to {}, groupBy: {}", fromDate, toDate, groupBy);
        SalesReportResponse response = reportsService.getSalesReport(groupBy, fromDate.toString(), toDate.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Get product-specific sales report
     */
    @GetMapping("/products")
    @Operation(summary = "Get product sales report", description = "Retrieves sales report by product for the specified period")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product sales report"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALES_MANAGER') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<ProductSalesReportResponse> getProductSalesReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Result limit") @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Getting product sales report from {} to {}, limit: {}", fromDate, toDate, limit);
        ProductSalesReportResponse response = reportsService.getProductSalesReport("DAILY", fromDate.toString(), toDate.toString(), null, null);
        return ResponseEntity.ok(response);
    }

    /**
     * Export sales report
     */
    @GetMapping("/export/sales")
    @Operation(summary = "Export sales report", description = "Exports sales report for the specified period in file format")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File export successful"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALES_MANAGER')")
    public ResponseEntity<byte[]> exportSalesReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Export format (CSV, EXCEL, PDF)") @RequestParam(defaultValue = "CSV") String format) {
        
        log.info("Exporting sales report from {} to {}, format: {}", fromDate, toDate, format);
        byte[] fileData = reportsService.exportSalesReport(fromDate, toDate, format);
        
        HttpHeaders headers = new HttpHeaders();
        String filename = String.format("sales_report_%s_%s.%s", fromDate, toDate, format.toLowerCase());
        headers.setContentDispositionFormData("attachment", filename);
        
        MediaType mediaType = switch (format.toUpperCase()) {
            case "CSV" -> MediaType.TEXT_PLAIN;
            case "EXCEL" -> MediaType.APPLICATION_OCTET_STREAM;
            case "PDF" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(fileData);
    }

    /**
     * Get shipping performance report
     */
    @GetMapping("/shipping")
    @Operation(summary = "Get shipping performance report", description = "Retrieves shipping performance report for the specified period")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved shipping performance report"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS_MANAGER')")
    public ResponseEntity<ShippingReportResponse> getShippingReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Carrier filter") @RequestParam(required = false) String carrier) {
        
        log.info("Getting shipping report from {} to {}, carrier: {}", fromDate, toDate, carrier);
        ShippingReportResponse response = reportsService.getShippingReport(fromDate.toString(), toDate.toString(), carrier);
        return ResponseEntity.ok(response);
    }

    /**
     * Get returns analysis report
     */
    @GetMapping("/returns")
    @Operation(summary = "Get returns analysis report", description = "Retrieves returns analysis report for the specified period")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved returns analysis report"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE') or hasRole('QUALITY_MANAGER')")
    public ResponseEntity<ReturnReportResponse> getReturnReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Return reason filter") @RequestParam(required = false) String reason) {
        
        log.info("Getting return report from {} to {}, reason: {}", fromDate, toDate, reason);
        ReturnReportResponse response = reportsService.getReturnReport(fromDate.toString(), toDate.toString(), reason);
        return ResponseEntity.ok(response);
    }
}
