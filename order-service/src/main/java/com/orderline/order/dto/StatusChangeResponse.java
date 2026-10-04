package com.orderline.order.dto;

import com.orderline.order.entity.OrderStatus;

import java.time.Instant;

public record StatusChangeResponse(
        OrderStatus status,
        Instant changedAt
) {
}
