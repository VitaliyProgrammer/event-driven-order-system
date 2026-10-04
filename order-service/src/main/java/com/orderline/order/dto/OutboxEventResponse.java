package com.orderline.order.dto;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record OutboxEventResponse(
        UUID id,
        UUID aggregateId,
        String eventType,
        Instant createdAt,
        @Nullable Instant publishedAt
) {
}
