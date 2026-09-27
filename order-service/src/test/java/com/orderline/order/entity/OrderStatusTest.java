package com.orderline.order.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @CsvSource({
            "CREATED, RESERVED",
            "CREATED, CANCELLED",
            "RESERVED, PAID",
            "RESERVED, CANCELLED",
            "PAID, SHIPPED",
            "SHIPPED, DELIVERED"
    })
    void allowsLifecycleTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} is rejected")
    @CsvSource({
            "CREATED, PAID",
            "CREATED, SHIPPED",
            "RESERVED, SHIPPED",
            "PAID, CANCELLED",
            "SHIPPED, CANCELLED",
            "DELIVERED, CANCELLED",
            "CANCELLED, RESERVED",
            "CANCELLED, PAID"
    })
    void rejectsTransitionsOutsideLifecycle(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }
}
