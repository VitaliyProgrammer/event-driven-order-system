package com.orderline.notification.service;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.EventTypes;
import com.orderline.common.event.InvalidEventException;
import com.orderline.common.event.OrderCreatedPayload;
import com.orderline.common.event.OrderStatusPayload;
import com.orderline.common.security.CurrentUser;
import com.orderline.notification.dto.NotificationResponse;
import com.orderline.notification.entity.Notification;
import com.orderline.notification.exception.NotificationNotFoundException;
import com.orderline.notification.mapper.NotificationMapper;
import com.orderline.notification.repository.NotificationRepository;
import com.orderline.notification.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final NotificationMapper notificationMapper;
    private final EventJsonMapper eventJsonMapper;

    @Transactional
    public Optional<Notification> process(EventEnvelope event) {
        if (!processedEventRepository.markProcessed(event.eventId())) {
            log.info("Event {} was already processed, skipping", event.eventId());
            return Optional.empty();
        }
        return buildMessage(event)
                .map(message -> notificationRepository.saveAndFlush(
                        new Notification(customerId(event), event.orderId(), event.type(), message)));
    }

    public Page<NotificationResponse> findMine(UUID customerId, Pageable pageable) {
        return notificationRepository.findByCustomerId(customerId, pageable).map(notificationMapper::toResponse);
    }

    @Transactional
    public NotificationResponse markRead(CurrentUser user, UUID id) {
        Notification notification = notificationRepository.findById(id)
                .filter(found -> user.canAccess(found.getCustomerId()))
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.markRead();
        return notificationMapper.toResponse(notification);
    }

    private Optional<String> buildMessage(EventEnvelope event) {
        UUID orderId = event.orderId();
        return switch (event.type()) {
            case EventTypes.ORDER_CREATED -> Optional.of("Order %s has been placed".formatted(orderId));
            case EventTypes.ORDER_PAID -> Optional.of("Order %s has been paid".formatted(orderId));
            case EventTypes.ORDER_SHIPPED -> Optional.of("Order %s has been shipped".formatted(orderId));
            case EventTypes.ORDER_DELIVERED -> Optional.of("Order %s has been delivered".formatted(orderId));
            case EventTypes.ORDER_CANCELLED -> Optional.of("Order %s has been cancelled: %s".formatted(orderId,
                    eventJsonMapper.payload(event, OrderStatusPayload.class).reason()));
            default -> Optional.empty();
        };
    }

    private UUID customerId(EventEnvelope event) {
        UUID customerId = EventTypes.ORDER_CREATED.equals(event.type())
                ? eventJsonMapper.payload(event, OrderCreatedPayload.class).customerId()
                : eventJsonMapper.payload(event, OrderStatusPayload.class).customerId();
        if (customerId == null) {
            throw new InvalidEventException("%s %s has no customerId".formatted(event.type(), event.eventId()));
        }
        return customerId;
    }
}
