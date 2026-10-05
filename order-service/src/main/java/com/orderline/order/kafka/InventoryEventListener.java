package com.orderline.order.kafka;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.EventTypes;
import com.orderline.common.event.InventoryPayload;
import com.orderline.common.event.Topics;
import com.orderline.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final EventJsonMapper eventJsonMapper;
    private final OrderService orderService;

    @KafkaListener(topics = Topics.INVENTORY_EVENTS)
    public void onInventoryEvent(String message) {
        EventEnvelope event = eventJsonMapper.read(message);
        log.info("Received {} {} for order {}", event.type(), event.eventId(), event.orderId());

        switch (event.type()) {
            case EventTypes.INVENTORY_RESERVED -> orderService.confirmReservation(event.orderId());
            case EventTypes.INVENTORY_REJECTED -> orderService.rejectReservation(event.orderId(),
                    eventJsonMapper.payload(event, InventoryPayload.class).reason());
            default -> log.debug("Ignoring event type {}", event.type());
        }
    }
}
