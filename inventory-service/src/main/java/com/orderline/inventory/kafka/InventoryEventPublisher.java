package com.orderline.inventory.kafka;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.InventoryPayload;
import com.orderline.common.event.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventJsonMapper eventJsonMapper;

    public void publish(String eventType, UUID orderId, @Nullable String reason) {
        EventEnvelope event = eventJsonMapper.create(eventType, orderId, new InventoryPayload(reason));
        kafkaTemplate.send(Topics.INVENTORY_EVENTS, orderId.toString(), eventJsonMapper.write(event)).join();
        log.info("Published {} {} for order {}", eventType, event.eventId(), orderId);
    }
}
