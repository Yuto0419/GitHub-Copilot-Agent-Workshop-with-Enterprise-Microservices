package com.skishop.sales.repository.jpa;

import com.skishop.sales.entity.jpa.Return;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Returns repository
 */
@Repository
public interface ReturnRepository extends JpaRepository<Return, UUID> {

    /**
     * Search by return number
     */
    Optional<Return> findByReturnNumber(String returnNumber);

    /**
     * Search returns by order ID
     */
    List<Return> findByOrderId(UUID orderId);

    /**
     * Find returns by order item ID
     */
    List<Return> findByOrderItemId(UUID orderItemId);

    /**
     * Find returns by customer ID
     */
    Page<Return> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Find returns by status
     */
    Page<Return> findByStatus(Return.ReturnStatus status, Pageable pageable);

    /**
     * Find returns by reason
     */
    Page<Return> findByReason(Return.ReturnReason reason, Pageable pageable);

    /**
     * Find returns by customer ID and status
     */
    Page<Return> findByCustomerIdAndStatus(String customerId, Return.ReturnStatus status, Pageable pageable);

    /**
     * Find returns by request period
     */
    Page<Return> findByRequestedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find by list of return statuses
     */
    @Query("SELECT r FROM Return r WHERE r.status IN :statuses")
    Page<Return> findByStatusIn(@Param("statuses") List<Return.ReturnStatus> statuses, Pageable pageable);

    /**
     * Find pending returns
     */
    @Query("SELECT r FROM Return r WHERE r.status = 'REQUESTED' ORDER BY r.requestedAt ASC")
    Page<Return> findPendingReturns(Pageable pageable);

    /**
     * Find expired return requests
     */
    @Query("SELECT r FROM Return r WHERE r.status = 'REQUESTED' AND r.requestedAt < :expiryTime")
    List<Return> findExpiredReturnRequests(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * Count returns by reason
     */
    @Query("SELECT r.reason, COUNT(r) FROM Return r GROUP BY r.reason")
    List<Object[]> countReturnsByReason();

    /**
     * Count returns by status
     */
    @Query("SELECT r.status, COUNT(r) FROM Return r GROUP BY r.status")
    List<Object[]> countReturnsByStatus();

    /**
     * Count return history for customer
     */
    long countByCustomerId(String customerId);

    /**
     * Count returns in specified period
     */
    @Query("SELECT COUNT(r) FROM Return r WHERE r.requestedAt BETWEEN :startDate AND :endDate")
    long countReturnsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Get total refunds in specified period
     */
    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Return r WHERE r.status = 'REFUNDED' AND r.refundedAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRefundsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Check if return number exists
     */
    boolean existsByReturnNumber(String returnNumber);

    /**
     * Find returns by list of order IDs
     */
    @Query("SELECT r FROM Return r WHERE r.orderId IN :orderIds")
    List<Return> findByOrderIdIn(@Param("orderIds") List<UUID> orderIds);

    /**
     * Get latest returns for customer
     */
    @Query("SELECT r FROM Return r WHERE r.customerId = :customerId ORDER BY r.requestedAt DESC")
    Page<Return> findLatestReturnsByCustomerId(@Param("customerId") String customerId, Pageable pageable);
}
