package com.orderline.order.dto;

import java.util.UUID;

public record DemoEventResponse(
        UUID eventId,
        String eventType,
        String topic,
        String key
) {
}
