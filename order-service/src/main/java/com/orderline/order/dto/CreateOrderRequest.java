package com.orderline.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        // Temporary: moves to the JWT token once authentication is added.
        @NotNull
        UUID customerId,

        @NotEmpty
        List<@NotNull @Valid OrderItemRequest> items
) {
}
