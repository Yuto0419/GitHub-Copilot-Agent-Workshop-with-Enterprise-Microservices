package com.skishop.sales.repository.jpa;

import com.skishop.sales.entity.jpa.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Order item repository
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * Find order items by order ID
     */
    List<OrderItem> findByOrderId(UUID orderId);

    /**
     * Find order items by order ID (with pagination)
     */
    Page<OrderItem> findByOrderId(UUID orderId, Pageable pageable);

    /**
     * Find order items by product ID
     */
    Page<OrderItem> findByProductId(String productId, Pageable pageable);

    /**
     * Find order items by product SKU
     */
    Page<OrderItem> findBySku(String sku, Pageable pageable);

    /**
     * Find order items by order ID list
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId IN :orderIds")
    List<OrderItem> findByOrderIdIn(@Param("orderIds") List<UUID> orderIds);

    /**
     * Aggregate sales quantity for a specific product
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.productId = :productId")
    Integer getTotalQuantitySoldByProductId(@Param("productId") String productId);

    /**
     * Aggregate sales quantity for a specific SKU
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.sku = :sku")
    Integer getTotalQuantitySoldBySku(@Param("sku") String sku);

    /**
     * Get product sales ranking
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQuantity, SUM(oi.subtotal) as totalSales " +
           "FROM OrderItem oi " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalSales DESC")
    List<Object[]> getProductSalesRanking(Pageable pageable);

    /**
     * Find order items with applied coupons
     */
    List<OrderItem> findByAppliedCouponIdIsNotNull();

    /**
     * Find order items with used points
     */
    List<OrderItem> findByUsedPointsGreaterThan(Integer points);

    /**
     * Count order items by order ID
     */
    long countByOrderId(UUID orderId);

    /**
     * Find order items with specific coupon applied
     */
    List<OrderItem> findByAppliedCouponId(String couponId);

    /**
     * Find order items by product ID list
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productId IN :productIds")
    List<OrderItem> findByProductIdIn(@Param("productIds") List<String> productIds);

    /**
     * Delete order items by order ID
     */
    void deleteByOrderId(UUID orderId);
}
