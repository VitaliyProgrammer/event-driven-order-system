package com.orderline.inventory.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void reserveAndReleaseChangeStock() {
        Product product = new Product("Keyboard", BigDecimal.TEN, 5);

        product.reserve(3);
        assertThat(product.getStock()).isEqualTo(2);

        product.release(3);
        assertThat(product.getStock()).isEqualTo(5);
    }

    @Test
    void cannotReserveMoreThanInStock() {
        Product product = new Product("Keyboard", BigDecimal.TEN, 1);

        assertThatThrownBy(() -> product.reserve(2)).isInstanceOf(IllegalStateException.class);
        assertThat(product.getStock()).isEqualTo(1);
    }
}
