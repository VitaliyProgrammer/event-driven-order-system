package com.orderline.order.service;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.EventTypes;
import com.orderline.common.event.OrderCreatedPayload;
import com.orderline.common.event.Topics;
import com.orderline.order.dto.DemoEventResponse;
import com.orderline.order.entity.OutboxEvent;
import com.orderline.order.exception.OrderNotFoundException;
import com.orderline.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventJsonMapper eventJsonMapper;

    @Transactional(readOnly = true)
    public DemoEventResponse duplicateLastEvent(UUID orderId) {
        OutboxEvent event = outboxEventRepository
                .findLastPublished(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        String key = orderId.toString();
        kafkaTemplate.send(Topics.ORDER_EVENTS, key, event.getPayload()).join();
        log.info("Demo: re-sent event {} ({}) for order {}", event.getId(), event.getEventType(), orderId);
        return new DemoEventResponse(event.getId(), event.getEventType(), Topics.ORDER_EVENTS, key);
    }

    public DemoEventResponse sendBrokenEvent() {
        UUID orderId = UUID.randomUUID();
        EventEnvelope event = eventJsonMapper.create(EventTypes.ORDER_CREATED, orderId,
                new OrderCreatedPayload(null, List.of()));
        String key = orderId.toString();
        kafkaTemplate.send(Topics.ORDER_EVENTS, key, eventJsonMapper.write(event)).join();
        log.info("Demo: sent broken event {} for order {}", event.eventId(), orderId);
        return new DemoEventResponse(event.eventId(), event.type(), Topics.ORDER_EVENTS, key);
    }
}
