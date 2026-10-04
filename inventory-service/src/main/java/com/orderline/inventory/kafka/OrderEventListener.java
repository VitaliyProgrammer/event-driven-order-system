package com.orderline.inventory.kafka;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.Topics;
import com.orderline.inventory.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final EventJsonMapper eventJsonMapper;
    private final ReservationService reservationService;

    @KafkaListener(topics = Topics.ORDER_EVENTS)
    public void onOrderEvent(String message) {
        EventEnvelope event = eventJsonMapper.read(message);
        log.info("Received {} {} for order {}", event.type(), event.eventId(), event.orderId());
        reservationService.process(event);
    }
}
