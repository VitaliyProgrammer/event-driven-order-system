package com.orderline.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Product(String name, BigDecimal price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public boolean hasInStock(int quantity) {
        return stock >= quantity;
    }

    public void reserve(int quantity) {
        if (!hasInStock(quantity)) {
            throw new IllegalStateException("Not enough stock of product %s: %d requested, %d available"
                    .formatted(id, quantity, stock));
        }
        stock -= quantity;
    }

    public void release(int quantity) {
        stock += quantity;
    }

    public void changeStock(int newStock) {
        stock = newStock;
    }
}
