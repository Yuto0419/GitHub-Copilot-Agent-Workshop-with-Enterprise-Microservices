package com.skishop.sales.service;

import com.skishop.sales.dto.request.OrderCreateRequest;
import com.skishop.sales.dto.request.OrderStatusUpdateRequest;
import com.skishop.sales.dto.response.OrderResponse;
import com.skishop.sales.entity.jpa.Order;
import com.skishop.sales.entity.jpa.OrderItem;
import com.skishop.sales.exception.ResourceNotFoundException;
import com.skishop.sales.exception.InvalidOrderStateException;
import com.skishop.sales.mapper.OrderMapper;
import com.skishop.sales.repository.jpa.OrderRepository;
import com.skishop.sales.repository.jpa.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final EventPublisherService eventPublisherService;

    /**
     * Create order
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("Creating order for customer: {}", request.customerId());

        // Generate order number
        String orderNumber = orderNumberGenerator.generate();

        // Create order entity
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.customerId())
                .orderDate(LocalDateTime.now())
                .status(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .couponCode(request.couponCode())
                .usedPoints(request.usedPoints())
                .notes(request.notes())
                .build();

        // Set shipping address
        if (request.shippingAddress() != null) {
            Order.ShippingAddress shippingAddress = new Order.ShippingAddress(
                    request.shippingAddress().postalCode(),
                    request.shippingAddress().prefecture(),
                    request.shippingAddress().city(),
                    request.shippingAddress().addressLine1(),
                    request.shippingAddress().addressLine2(),
                    request.shippingAddress().recipientName(),
                    request.shippingAddress().phoneNumber()
            );
            order.setShippingAddress(shippingAddress);
        }

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Create order items
        List<OrderItem> orderItems = request.items().stream()
                .map(itemRequest -> {
                    OrderItem orderItem = OrderItem.builder()
                            .orderId(savedOrder.getId())
                            .productId(itemRequest.productId())
                            .productName(itemRequest.productName())
                            .sku(itemRequest.sku())
                            .unitPrice(itemRequest.unitPrice())
                            .quantity(itemRequest.quantity())
                            .build();
                    orderItem.calculateSubtotal();
                    return orderItem;
                })
                .toList();

        orderItemRepository.saveAll(orderItems);

        // Calculate amounts
        calculateOrderAmounts(savedOrder, orderItems);
        orderRepository.save(savedOrder);

        // Publish event
        eventPublisherService.publishOrderCreatedEvent(savedOrder, orderItems);

        log.info("Order created successfully: {}", orderNumber);
        return orderMapper.toResponse(savedOrder, orderItems);
    }

    /**
     * Get order
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderMapper.toResponse(order, orderItems);
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        return orderMapper.toResponse(order, orderItems);
    }

    /**
     * Get customer orders
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomer(String customerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        return orders.map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            return orderMapper.toResponse(order, orderItems);
        });
    }

    /**
     * Update order status
     */
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(request.status());
        
        // Validate status transition validity
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        if (request.notes() != null) {
            order.setNotes(order.getNotes() + "\n" + request.notes());
        }

        Order savedOrder = orderRepository.save(order);

        // Publish event
        eventPublisherService.publishOrderStatusUpdatedEvent(savedOrder);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderMapper.toResponse(savedOrder, orderItems);
    }

    /**
     * Cancel order
     */
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!canCancelOrder(order.getStatus())) {
            throw new InvalidOrderStateException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setNotes(order.getNotes() + "\nCancelled: " + reason);

        Order savedOrder = orderRepository.save(order);

        // Publish event
        eventPublisherService.publishOrderCancelledEvent(savedOrder);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderMapper.toResponse(savedOrder, orderItems);
    }

    /**
     * Calculate order amounts
     */
    private void calculateOrderAmounts(Order order, List<OrderItem> orderItems) {
        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // TODO: The specification of the tax rate should be set by an external variable rather than being static.
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax rate
        BigDecimal shippingFee = calculateShippingFee(subtotal);
        BigDecimal discountAmount = calculateDiscountAmount(order);

        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(taxAmount);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(subtotal.add(taxAmount).add(shippingFee).subtract(discountAmount));
    }

    /**
     * Shipping fee calculation
     * Uses Java 21's Switch expressions to implement tiered shipping fee calculation
     */
    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        return switch (subtotal.compareTo(BigDecimal.ZERO)) {
            case -1 -> BigDecimal.ZERO; // For negative values
            case 0 -> BigDecimal.valueOf(800); // For 0 JPY
            default -> switch (subtotal.compareTo(BigDecimal.valueOf(5000))) {
                case -1 -> BigDecimal.valueOf(500); // Less than 5000 JPY
                default -> switch (subtotal.compareTo(BigDecimal.valueOf(10000))) {
                    case -1 -> BigDecimal.valueOf(300); // 5000 JPY or more, less than 10000 JPY
                    default -> BigDecimal.ZERO; // Free shipping for 10000 JPY or more
                };
            };
        };
    }

    /**
     * Calculate discount amount
     */
    private BigDecimal calculateDiscountAmount(Order order) {
        BigDecimal discount = BigDecimal.ZERO;

        // Points discount
        if (order.getUsedPoints() != null && order.getUsedPoints() > 0) {
            BigDecimal pointDiscount = BigDecimal.valueOf(order.getUsedPoints());
            order.setPointDiscountAmount(pointDiscount);
            discount = discount.add(pointDiscount);
        }

        return discount;
    }

    /**
     * Validate status transition
     * Using Java 21 pattern matching and Switch expressions
     */
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        boolean isValidTransition = switch (currentStatus) {
            case CANCELLED -> newStatus == Order.OrderStatus.CANCELLED;
            case DELIVERED -> newStatus == Order.OrderStatus.RETURNED || newStatus == Order.OrderStatus.DELIVERED;
            case PENDING -> newStatus != Order.OrderStatus.DELIVERED;
            case CONFIRMED -> newStatus != Order.OrderStatus.PENDING;
            case PROCESSING -> newStatus != Order.OrderStatus.PENDING && newStatus != Order.OrderStatus.CONFIRMED;
            case SHIPPED -> newStatus == Order.OrderStatus.DELIVERED || newStatus == Order.OrderStatus.RETURNED || newStatus == Order.OrderStatus.SHIPPED;
            case RETURNED -> newStatus == Order.OrderStatus.RETURNED;
        };

        if (!isValidTransition) {
            throw new InvalidOrderStateException("Cannot change status from " + currentStatus + " to " + newStatus);
        }
    }

    /**
     * Check if order can be cancelled
     * Using Java 21 Switch expressions for more concise code
     */
    private boolean canCancelOrder(Order.OrderStatus status) {
        return switch (status) {
            case PENDING, CONFIRMED -> true;
            case PROCESSING, SHIPPED, DELIVERED, CANCELLED, RETURNED -> false;
        };
    }
}
