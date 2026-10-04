package com.orderline.inventory.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @PositiveOrZero
        int stock
) {
}
