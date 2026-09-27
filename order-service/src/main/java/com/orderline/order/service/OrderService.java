package com.orderline.order.service;

import com.orderline.order.dto.CreateOrderRequest;
import com.orderline.order.dto.OrderResponse;
import com.orderline.order.entity.Order;
import com.orderline.order.entity.OrderStatus;
import com.orderline.order.exception.OrderNotFoundException;
import com.orderline.order.mapper.OrderMapper;
import com.orderline.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Order order = Order.create(request.customerId());
        request.items().forEach(item -> order.addItem(item.productId(), item.quantity()));
        return orderMapper.toResponse(orderRepository.save(order));
    }

    public OrderResponse getById(UUID id) {
        return orderMapper.toResponse(findWithItems(id));
    }

    public Page<OrderResponse> getCustomerOrders(UUID customerId, @Nullable OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
                ? orderRepository.findByCustomerId(customerId, pageable)
                : orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        return orders.map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        Order order = findWithItems(id);
        order.cancel();
        // Flush now so the response carries the new updatedAt/version
        // and a concurrent-update conflict surfaces here, not after the method returns.
        return orderMapper.toResponse(orderRepository.saveAndFlush(order));
    }

    private Order findWithItems(UUID id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
