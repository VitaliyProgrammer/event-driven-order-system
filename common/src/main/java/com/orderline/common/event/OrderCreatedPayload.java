package com.orderline.common.event;

import java.util.List;
import java.util.UUID;

public record OrderCreatedPayload(
        UUID customerId,
        List<OrderLine> items
) {
}
