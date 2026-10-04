package com.orderline.notification.kafka;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.Topics;
import com.orderline.notification.mapper.NotificationMapper;
import com.orderline.notification.service.NotificationService;
import com.orderline.notification.service.NotificationStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final EventJsonMapper eventJsonMapper;
    private final NotificationService notificationService;
    private final NotificationStream notificationStream;
    private final NotificationMapper notificationMapper;

    @KafkaListener(topics = Topics.ORDER_EVENTS)
    public void onOrderEvent(String message) {
        EventEnvelope event = eventJsonMapper.read(message);
        log.info("Received {} {} for order {}", event.type(), event.eventId(), event.orderId());
        notificationService.process(event).ifPresent(notification -> notificationStream.send(
                notification.getCustomerId(), notificationMapper.toResponse(notification)));
    }
}
