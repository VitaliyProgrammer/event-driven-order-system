package com.orderline.order.repository;

import com.orderline.order.entity.Order;
import com.orderline.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // One query with a JOIN instead of a second SELECT for the items.
    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(UUID id);

    // Items of a page are loaded in batches (hibernate.default_batch_fetch_size), not one query per order.
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status, Pageable pageable);
}
