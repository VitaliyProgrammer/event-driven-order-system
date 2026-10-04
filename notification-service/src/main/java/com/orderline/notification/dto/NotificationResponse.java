package com.orderline.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID orderId,
        String eventType,
        String message,
        boolean read,
        Instant createdAt
) {
}
