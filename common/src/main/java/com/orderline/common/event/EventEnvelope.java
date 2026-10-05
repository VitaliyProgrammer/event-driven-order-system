package com.orderline.common.event;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
        UUID eventId,
        String type,
        int version,
        Instant occurredAt,
        UUID orderId,
        JsonNode payload
) {
}
