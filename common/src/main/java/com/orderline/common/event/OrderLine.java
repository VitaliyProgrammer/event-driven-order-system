package com.orderline.common.event;

import java.util.UUID;

public record OrderLine(
        UUID productId,
        int quantity
) {
}
