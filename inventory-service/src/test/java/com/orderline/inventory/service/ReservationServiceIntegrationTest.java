package com.orderline.inventory.service;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.EventTypes;
import com.orderline.common.event.OrderCreatedPayload;
import com.orderline.common.event.OrderLine;
import com.orderline.common.event.OrderStatusPayload;
import com.orderline.inventory.entity.Product;
import com.orderline.inventory.entity.ReservationStatus;
import com.orderline.inventory.repository.ProductRepository;
import com.orderline.inventory.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ReservationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:4.1.0");

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventJsonMapper eventJsonMapper;

    @Test
    void sameEventProcessedTwiceReservesOnlyOnce() {
        Product product = productRepository.save(new Product("Keyboard", BigDecimal.TEN, 10));
        UUID orderId = UUID.randomUUID();
        EventEnvelope event = orderCreated(orderId, product.getId(), 3);

        reservationService.process(event);
        reservationService.process(event);

        assertThat(stockOf(product)).isEqualTo(7);
        assertThat(reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED)).hasSize(1);
    }

    @Test
    void notEnoughStockReservesNothing() {
        Product product = productRepository.save(new Product("Monitor", BigDecimal.TEN, 1));
        UUID orderId = UUID.randomUUID();

        reservationService.process(orderCreated(orderId, product.getId(), 2));

        assertThat(stockOf(product)).isEqualTo(1);
        assertThat(reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED)).isEmpty();
    }

    @Test
    void cancellationReturnsReservedStock() {
        Product product = productRepository.save(new Product("Mouse", BigDecimal.TEN, 5));
        UUID orderId = UUID.randomUUID();
        reservationService.process(orderCreated(orderId, product.getId(), 4));

        reservationService.process(eventJsonMapper.create(EventTypes.ORDER_CANCELLED, orderId,
                new OrderStatusPayload(UUID.randomUUID(), "Payment timeout")));

        assertThat(stockOf(product)).isEqualTo(5);
        assertThat(reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RELEASED)).hasSize(1);
    }

    private EventEnvelope orderCreated(UUID orderId, UUID productId, int quantity) {
        return eventJsonMapper.create(EventTypes.ORDER_CREATED, orderId,
                new OrderCreatedPayload(UUID.randomUUID(), List.of(new OrderLine(productId, quantity))));
    }

    private int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getStock();
    }
}
