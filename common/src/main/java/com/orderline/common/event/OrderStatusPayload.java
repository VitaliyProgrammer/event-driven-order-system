package com.orderline.common.event;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record OrderStatusPayload(
        UUID customerId,
        @Nullable String reason
) {
}
