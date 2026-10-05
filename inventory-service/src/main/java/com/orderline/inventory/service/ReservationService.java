package com.orderline.inventory.service;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.common.event.EventTypes;
import com.orderline.common.event.InvalidEventException;
import com.orderline.common.event.OrderCreatedPayload;
import com.orderline.common.event.OrderLine;
import com.orderline.inventory.entity.Product;
import com.orderline.inventory.entity.Reservation;
import com.orderline.inventory.entity.ReservationStatus;
import com.orderline.inventory.kafka.InventoryEventPublisher;
import com.orderline.inventory.repository.ProcessedEventRepository;
import com.orderline.inventory.repository.ProductRepository;
import com.orderline.inventory.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ProcessedEventRepository processedEventRepository;
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryEventPublisher eventPublisher;
    private final EventJsonMapper eventJsonMapper;

    @Transactional
    public void process(EventEnvelope event) {
        if (!processedEventRepository.markProcessed(event.eventId())) {
            log.info("Event {} was already processed, skipping", event.eventId());
            return;
        }
        switch (event.type()) {
            case EventTypes.ORDER_CREATED ->
                    reserve(event.orderId(), eventJsonMapper.payload(event, OrderCreatedPayload.class));
            case EventTypes.ORDER_PAID -> confirm(event.orderId());
            case EventTypes.ORDER_CANCELLED -> release(event.orderId());
            default -> log.debug("Ignoring event type {}", event.type());
        }
    }

    private void reserve(UUID orderId, OrderCreatedPayload payload) {
        Map<UUID, Integer> requested = requestedQuantities(orderId, payload);
        Map<UUID, Product> products = productRepository.findAllForUpdate(requested.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Optional<String> shortage = findShortage(requested, products);
        if (shortage.isPresent()) {
            eventPublisher.publish(EventTypes.INVENTORY_REJECTED, orderId, shortage.get());
            return;
        }

        requested.forEach((productId, quantity) -> {
            products.get(productId).reserve(quantity);
            reservationRepository.save(new Reservation(orderId, productId, quantity));
        });
        reservationRepository.flush();
        eventPublisher.publish(EventTypes.INVENTORY_RESERVED, orderId, null);
    }

    private void confirm(UUID orderId) {
        reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED)
                .forEach(Reservation::confirm);
    }

    private void release(UUID orderId) {
        for (Reservation reservation : reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED)) {
            productRepository.findForUpdate(reservation.getProductId())
                    .ifPresent(product -> product.release(reservation.getQuantity()));
            reservation.release();
        }
    }

    private static Map<UUID, Integer> requestedQuantities(UUID orderId, OrderCreatedPayload payload) {
        if (payload.items() == null || payload.items().isEmpty()) {
            throw new InvalidEventException("OrderCreated for order %s has no items".formatted(orderId));
        }
        for (OrderLine line : payload.items()) {
            if (line.productId() == null || line.quantity() <= 0) {
                throw new InvalidEventException("OrderCreated for order %s has an invalid line %s".formatted(orderId, line));
            }
        }
        return payload.items().stream()
                .collect(Collectors.toMap(OrderLine::productId, OrderLine::quantity, Integer::sum));
    }

    private static Optional<String> findShortage(Map<UUID, Integer> requested, Map<UUID, Product> products) {
        for (Map.Entry<UUID, Integer> entry : requested.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product == null) {
                return Optional.of("Product %s does not exist".formatted(entry.getKey()));
            }
            if (!product.hasInStock(entry.getValue())) {
                return Optional.of("Not enough stock of %s: %d requested, %d available"
                        .formatted(product.getName(), entry.getValue(), product.getStock()));
            }
        }
        return Optional.empty();
    }
}
