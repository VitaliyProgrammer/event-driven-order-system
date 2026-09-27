package com.orderline.order.exception;

import com.orderline.order.entity.OrderStatus;

import java.util.UUID;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(UUID orderId, OrderStatus current, OrderStatus target) {
        super("Order %s cannot move from %s to %s".formatted(orderId, current, target));
    }
}
