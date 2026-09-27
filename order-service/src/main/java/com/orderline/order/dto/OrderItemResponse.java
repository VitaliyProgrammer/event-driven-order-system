package com.orderline.order.dto;

import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        int quantity
) {
}
