package com.orderline.inventory.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateStockRequest(

        @PositiveOrZero
        int stock
) {
}
