package com.orderline.order.entity;

import com.orderline.order.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void newOrderStartsAsCreatedWithItems() {
        UUID productId = UUID.randomUUID();
        Order order = Order.create(UUID.randomUUID());

        order.addItem(productId, 2);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getItems())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getProductId()).isEqualTo(productId);
                    assertThat(item.getQuantity()).isEqualTo(2);
                    assertThat(item.getOrder()).isSameAs(order);
                });
    }

    @Test
    void goesThroughFullLifecycle() {
        Order order = Order.create(UUID.randomUUID());

        order.markReserved();
        order.pay();
        order.ship();
        order.deliver();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void cannotPayOrderThatIsNotReserved() {
        Order order = Order.create(UUID.randomUUID());

        assertThatThrownBy(order::pay)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("from CREATED to PAID");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void cannotCancelPaidOrder() {
        Order order = Order.create(UUID.randomUUID());
        order.markReserved();
        order.pay();

        assertThatThrownBy(order::cancel).isInstanceOf(InvalidOrderStateException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }
}
