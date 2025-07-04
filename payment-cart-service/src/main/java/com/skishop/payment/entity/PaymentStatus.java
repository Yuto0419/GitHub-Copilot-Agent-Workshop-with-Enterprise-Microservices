package com.skishop.payment.entity;

/**
 * Payment status
 * Improved version using Java 21 sealed class
 */
public sealed interface PaymentStatus 
    permits PaymentStatus.Pending, 
            PaymentStatus.RequiresAction, 
            PaymentStatus.Confirmed, 
            PaymentStatus.Completed, 
            PaymentStatus.Failed, 
            PaymentStatus.Cancelled, 
            PaymentStatus.Refunded, 
            PaymentStatus.PartiallyRefunded {

    /**
     * Get display name
     */
    String getDisplayName();

    /**
     * Whether the payment is being processed
     */
    default boolean isProcessing() {
        return this instanceof Pending || this instanceof RequiresAction;
    }

    /**
     * Whether the payment is completed
     */
    default boolean isCompleted() {
        return this instanceof Completed || this instanceof Refunded || this instanceof PartiallyRefunded;
    }

    /**
     * Whether the payment is in a failed state
     */
    default boolean isFailed() {
        return this instanceof Failed || this instanceof Cancelled;
    }

    record Pending() implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Processing";
        }
    }

    record RequiresAction() implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Action Required";
        }
    }

    record Confirmed() implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Confirmed";
        }
    }

    record Completed() implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Completed";
        }
    }

    record Failed(String reason) implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Failed";
        }
    }

    record Cancelled(String reason) implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Cancelled";
        }
    }

    record Refunded() implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Refunded";
        }
    }

    record PartiallyRefunded() implements PaymentStatus {
        @Override
        public String getDisplayName() {
            return "Partially Refunded";
        }
    }

    /**
     * Create PaymentStatus from string
     */
    static PaymentStatus fromString(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> new Pending();
            case "REQUIRES_ACTION" -> new RequiresAction();
            case "CONFIRMED" -> new Confirmed();
            case "COMPLETED" -> new Completed();
            case "FAILED" -> new Failed(null);
            case "CANCELLED" -> new Cancelled(null);
            case "REFUNDED" -> new Refunded();
            case "PARTIALLY_REFUNDED" -> new PartiallyRefunded();
            default -> throw new IllegalArgumentException("Unknown payment status: " + status);
        };
    }

    /**
     * Convert PaymentStatus to string
     */
    default String toStatusString() {
        return switch (this) {
            case Pending() -> "PENDING";
            case RequiresAction() -> "REQUIRES_ACTION";
            case Confirmed() -> "CONFIRMED";
            case Completed() -> "COMPLETED";
            case Failed(var reason) -> "FAILED";
            case Cancelled(var reason) -> "CANCELLED";
            case Refunded() -> "REFUNDED";
            case PartiallyRefunded() -> "PARTIALLY_REFUNDED";
        };
    }
}
