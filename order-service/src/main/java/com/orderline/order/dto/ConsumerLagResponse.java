package com.orderline.order.dto;

public record ConsumerLagResponse(
        String groupId,
        String topic,
        int partition,
        long committedOffset,
        long endOffset,
        long lag
) {
}
