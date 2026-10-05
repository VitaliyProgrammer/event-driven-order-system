package com.orderline.order.entity;

public enum OrderStatus {

    CREATED,
    RESERVED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /**
     * The single source of truth for the order lifecycle.
     * The switch is exhaustive: adding a new status will not compile until its transitions are defined here.
     */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case CREATED -> next == RESERVED || next == CANCELLED;
            case RESERVED -> next == PAID || next == CANCELLED;
            case PAID -> next == SHIPPED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
