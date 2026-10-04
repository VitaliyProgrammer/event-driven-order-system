package com.orderline.order.service;

import com.orderline.common.event.EventTypes;
import com.orderline.common.event.OrderCreatedPayload;
import com.orderline.common.event.OrderLine;
import com.orderline.common.event.OrderStatusPayload;
import com.orderline.common.security.CurrentUser;
import com.orderline.order.dto.CreateOrderRequest;
import com.orderline.order.dto.OrderResponse;
import com.orderline.order.dto.StatusChangeResponse;
import com.orderline.order.entity.Order;
import com.orderline.order.entity.OrderStatus;
import com.orderline.order.entity.OrderStatusChange;
import com.orderline.order.exception.OrderNotFoundException;
import com.orderline.order.mapper.OrderMapper;
import com.orderline.order.repository.OrderRepository;
import com.orderline.order.repository.OrderStatusChangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final String CANCELLED_BY_CUSTOMER = "Cancelled by customer";
    private static final String CANCELLED_BY_PAYMENT_TIMEOUT = "Payment timeout";

    private final OrderRepository orderRepository;
    private final OrderStatusChangeRepository statusChangeRepository;
    private final OutboxService outboxService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(UUID customerId, CreateOrderRequest request) {
        Order order = Order.create(customerId);
        request.items().forEach(item -> order.addItem(item.productId(), item.quantity()));
        orderRepository.saveAndFlush(order);
        recordStatus(order);

        List<OrderLine> lines = order.getItems().stream()
                .map(item -> new OrderLine(item.getProductId(), item.getQuantity()))
                .toList();
        outboxService.add(EventTypes.ORDER_CREATED, order.getId(), new OrderCreatedPayload(customerId, lines));
        return orderMapper.toResponse(order);
    }

    public OrderResponse getById(CurrentUser user, UUID id) {
        return orderMapper.toResponse(findAccessible(user, id));
    }

    public Page<OrderResponse> getCustomerOrders(UUID customerId, @Nullable OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
                ? orderRepository.findByCustomerId(customerId, pageable)
                : orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        return orders.map(orderMapper::toResponse);
    }

    public List<StatusChangeResponse> getTimeline(CurrentUser user, UUID id) {
        findAccessible(user, id);
        return statusChangeRepository.findTimeline(id).stream()
                .map(orderMapper::toStatusChangeResponse)
                .toList();
    }

    @Transactional
    public OrderResponse pay(CurrentUser user, UUID id) {
        Order order = findAccessible(user, id);
        order.pay();
        return publishStatusChange(order, EventTypes.ORDER_PAID, null);
    }

    @Transactional
    public OrderResponse cancel(CurrentUser user, UUID id) {
        Order order = findAccessible(user, id);
        order.cancel();
        return publishStatusChange(order, EventTypes.ORDER_CANCELLED, CANCELLED_BY_CUSTOMER);
    }

    @Transactional
    public OrderResponse ship(UUID id) {
        Order order = findWithItems(id);
        order.ship();
        return publishStatusChange(order, EventTypes.ORDER_SHIPPED, null);
    }

    @Transactional
    public OrderResponse deliver(UUID id) {
        Order order = findWithItems(id);
        order.deliver();
        return publishStatusChange(order, EventTypes.ORDER_DELIVERED, null);
    }

    @Transactional
    public void confirmReservation(UUID id) {
        Order order = findWithItems(id);
        if (order.getStatus() != OrderStatus.CREATED) {
            log.info("Order {} is {}, ignoring inventory reservation", id, order.getStatus());
            return;
        }
        order.markReserved();
        orderRepository.saveAndFlush(order);
        recordStatus(order);
    }

    @Transactional
    public void rejectReservation(UUID id, @Nullable String reason) {
        Order order = findWithItems(id);
        if (order.getStatus() != OrderStatus.CREATED) {
            log.info("Order {} is {}, ignoring inventory rejection", id, order.getStatus());
            return;
        }
        order.cancel();
        publishStatusChange(order, EventTypes.ORDER_CANCELLED, reason);
    }

    public List<UUID> findUnpaidIds(Instant reservedBefore) {
        return orderRepository.findStaleIds(OrderStatus.RESERVED, reservedBefore);
    }

    @Transactional
    public boolean cancelUnpaid(UUID id) {
        Order order = findWithItems(id);
        if (order.getStatus() != OrderStatus.RESERVED) {
            return false;
        }
        order.cancel();
        publishStatusChange(order, EventTypes.ORDER_CANCELLED, CANCELLED_BY_PAYMENT_TIMEOUT);
        return true;
    }

    private OrderResponse publishStatusChange(Order order, String eventType, @Nullable String reason) {
        orderRepository.saveAndFlush(order);
        recordStatus(order);
        outboxService.add(eventType, order.getId(), new OrderStatusPayload(order.getCustomerId(), reason));
        return orderMapper.toResponse(order);
    }

    private void recordStatus(Order order) {
        statusChangeRepository.save(new OrderStatusChange(order.getId(), order.getStatus()));
    }

    private Order findAccessible(CurrentUser user, UUID id) {
        Order order = findWithItems(id);
        if (!user.canAccess(order.getCustomerId())) {
            throw new OrderNotFoundException(id);
        }
        return order;
    }

    private Order findWithItems(UUID id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
