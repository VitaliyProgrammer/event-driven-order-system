package com.orderline.order.dto;

import java.util.List;

public record KafkaOverviewResponse(
        List<PartitionOffsetResponse> partitions,
        List<ConsumerLagResponse> consumers,
        List<OutboxEventResponse> recentEvents
) {
}
