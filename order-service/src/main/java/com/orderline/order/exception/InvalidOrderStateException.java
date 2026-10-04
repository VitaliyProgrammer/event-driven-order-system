package com.orderline.order.exception;

import com.orderline.common.web.ConflictException;
import com.orderline.order.entity.OrderStatus;

import java.util.UUID;

public class InvalidOrderStateException extends ConflictException {

    public InvalidOrderStateException(UUID orderId, OrderStatus current, OrderStatus target) {
        super("Order %s cannot move from %s to %s".formatted(orderId, current, target));
    }
}
