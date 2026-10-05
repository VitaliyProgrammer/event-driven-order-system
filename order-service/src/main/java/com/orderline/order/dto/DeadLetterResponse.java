package com.orderline.order.dto;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record DeadLetterResponse(
        String topic,
        int partition,
        long offset,
        @Nullable String key,
        @Nullable String originalTopic,
        @Nullable String error,
        @Nullable String payload,
        Instant failedAt
) {
}
