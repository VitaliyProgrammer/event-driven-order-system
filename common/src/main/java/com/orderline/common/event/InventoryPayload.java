package com.orderline.common.event;

import org.jspecify.annotations.Nullable;

public record InventoryPayload(
        @Nullable String reason
) {
}
