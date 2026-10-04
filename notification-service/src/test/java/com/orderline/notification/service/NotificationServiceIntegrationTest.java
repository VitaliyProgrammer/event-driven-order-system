package com.orderline.notification.service;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.EventTypes;
import com.orderline.common.event.InvalidEventException;
import com.orderline.common.event.OrderCreatedPayload;
import com.orderline.common.event.OrderStatusPayload;
import com.orderline.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class NotificationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:4.1.0");

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EventJsonMapper eventJsonMapper;

    @Test
    void duplicateEventCreatesOneNotification() {
        UUID customerId = UUID.randomUUID();
        EventEnvelope event = eventJsonMapper.create(EventTypes.ORDER_CANCELLED, UUID.randomUUID(),
                new OrderStatusPayload(customerId, "Cancelled by customer"));

        assertThat(notificationService.process(event)).isPresent();
        assertThat(notificationService.process(event)).isEmpty();

        assertThat(notificationRepository.findByCustomerId(customerId, Pageable.unpaged()))
                .singleElement()
                .satisfies(notification -> assertThat(notification.getMessage()).endsWith("Cancelled by customer"));
    }

    @Test
    void eventWithoutCustomerIsRejected() {
        EventEnvelope event = eventJsonMapper.create(EventTypes.ORDER_CREATED, UUID.randomUUID(),
                new OrderCreatedPayload(null, List.of()));

        assertThatThrownBy(() -> notificationService.process(event)).isInstanceOf(InvalidEventException.class);
    }
}
