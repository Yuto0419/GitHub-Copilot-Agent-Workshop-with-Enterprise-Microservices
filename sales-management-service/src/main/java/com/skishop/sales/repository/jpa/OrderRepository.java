package com.skishop.sales.repository.jpa;

import com.skishop.sales.entity.jpa.Order;
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
 * Order Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer ID
     */
    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Find by order status
     */
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    /**
     * Find by customer ID and order status
     */
    Page<Order> findByCustomerIdAndStatus(String customerId, Order.OrderStatus status, Pageable pageable);

    /**
     * Find by order date range
     */
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find by customer ID and order date range
     */
    Page<Order> findByCustomerIdAndOrderDateBetween(
        String customerId, 
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );

    /**
     * Find by list of order statuses
     */
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    Page<Order> findByStatusIn(@Param("statuses") List<Order.OrderStatus> statuses, Pageable pageable);

    /**
     * Get latest orders for customer
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC")
    Page<Order> findLatestOrdersByCustomerId(@Param("customerId") String customerId, Pageable pageable);

    /**
     * Count orders in specified period
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Get total sales in specified period
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED'")
    java.math.BigDecimal getTotalSalesBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count order history for customer
     */
    long countByCustomerId(String customerId);

    /**
     * Find by payment status
     */
    Page<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find by customer ID and list of order statuses
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status IN :statuses ORDER BY o.orderDate DESC")
    Page<Order> findByCustomerIdAndStatusIn(
        @Param("customerId") String customerId, 
        @Param("statuses") List<Order.OrderStatus> statuses, 
        Pageable pageable
    );

    /**
     * Find expired unpaid orders
     */
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'PENDING' AND o.createdAt < :expiryTime")
    List<Order> findExpiredPendingOrders(@Param("expiryTime") LocalDateTime expiryTime);
}
