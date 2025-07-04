package com.skishop.sales.dto.response;

import java.util.List;

/**
 * Return list response
 */
public record ReturnListResponse(
    List<ReturnSummary> returns,
    Integer totalCount,
    Integer totalPages,
    Integer currentPage,
    Integer pageSize
) {
    public record ReturnSummary(
        String returnId,
        String orderId,
        String userId,
        String status,
        String refundAmount,
        String requestedAt,
        String processedAt
    ) {}
}
