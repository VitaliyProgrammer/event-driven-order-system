package com.orderline.inventory.exception;

import com.orderline.common.web.NotFoundException;

import java.util.UUID;

public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(UUID productId) {
        super("Product %s not found".formatted(productId));
    }
}
