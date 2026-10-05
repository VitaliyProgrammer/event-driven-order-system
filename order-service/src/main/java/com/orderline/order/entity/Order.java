package com.orderline.order.entity;

import com.orderline.order.exception.InvalidOrderStateException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Optimistic locking: two concurrent updates of the same order cannot silently overwrite each other.
    @Version
    private long version;

    public static Order create(UUID customerId) {
        Order order = new Order();
        order.customerId = customerId;
        order.status = OrderStatus.CREATED;
        return order;
    }

    public void addItem(UUID productId, int quantity) {
        items.add(new OrderItem(this, productId, quantity));
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void markReserved() {
        transitionTo(OrderStatus.RESERVED);
    }

    public void pay() {
        transitionTo(OrderStatus.PAID);
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    private void transitionTo(OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidOrderStateException(id, status, next);
        }
        status = next;
    }
}
