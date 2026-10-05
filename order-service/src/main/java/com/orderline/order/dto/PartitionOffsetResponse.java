package com.orderline.order.dto;

public record PartitionOffsetResponse(
        String topic,
        int partition,
        long endOffset
) {
}
