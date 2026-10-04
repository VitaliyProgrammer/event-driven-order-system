package com.orderline.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventJsonMapper {

    private static final int CURRENT_VERSION = 1;

    private final JsonMapper jsonMapper;

    public EventEnvelope create(String type, UUID orderId, Object payload) {
        return new EventEnvelope(UUID.randomUUID(), type, CURRENT_VERSION, Instant.now(), orderId,
                jsonMapper.valueToTree(payload));
    }

    public String write(EventEnvelope event) {
        return jsonMapper.writeValueAsString(event);
    }

    public EventEnvelope read(String json) {
        EventEnvelope event = jsonMapper.readValue(json, EventEnvelope.class);
        if (event.eventId() == null || event.type() == null || event.orderId() == null || event.payload() == null) {
            throw new InvalidEventException("Event envelope is incomplete: " + json);
        }
        return event;
    }

    public <T> T payload(EventEnvelope event, Class<T> payloadType) {
        return jsonMapper.treeToValue(event.payload(), payloadType);
    }
}
