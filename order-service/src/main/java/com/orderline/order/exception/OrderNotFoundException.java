package com.orderline.order.exception;

import com.orderline.common.web.NotFoundException;

import java.util.UUID;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(UUID orderId) {
        super("Order %s not found".formatted(orderId));
    }
}
