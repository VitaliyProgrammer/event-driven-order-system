package com.orderline.order.service;

import com.orderline.common.event.Topics;
import com.orderline.order.dto.ConsumerLagResponse;
import com.orderline.order.dto.KafkaOverviewResponse;
import com.orderline.order.dto.PartitionOffsetResponse;
import com.orderline.order.mapper.OutboxEventMapper;
import com.orderline.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KafkaMonitorService {

    private static final List<String> TOPICS = List.of(
            Topics.ORDER_EVENTS, Topics.INVENTORY_EVENTS, Topics.ORDER_EVENTS_DLT, Topics.INVENTORY_EVENTS_DLT);
    private static final List<String> CONSUMER_GROUPS = List.of(
            "order-service", "inventory-service", "notification-service");
    private static final long TIMEOUT_SECONDS = 5;

    private final KafkaAdmin kafkaAdmin;
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventMapper outboxEventMapper;

    @Transactional(readOnly = true)
    public KafkaOverviewResponse getOverview() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Map<TopicPartition, Long> endOffsets = findEndOffsets(adminClient);

            List<PartitionOffsetResponse> partitions = endOffsets.entrySet().stream()
                    .map(entry -> new PartitionOffsetResponse(
                            entry.getKey().topic(), entry.getKey().partition(), entry.getValue()))
                    .sorted(Comparator.comparing(PartitionOffsetResponse::topic)
                            .thenComparing(PartitionOffsetResponse::partition))
                    .toList();

            List<ConsumerLagResponse> consumers = new ArrayList<>();
            for (String groupId : CONSUMER_GROUPS) {
                consumers.addAll(findConsumerLag(adminClient, groupId, endOffsets));
            }

            return new KafkaOverviewResponse(partitions, consumers,
                    outboxEventRepository.findRecent().stream()
                            .map(outboxEventMapper::toResponse)
                            .toList());
        }
    }

    private Map<TopicPartition, Long> findEndOffsets(AdminClient adminClient) {
        Map<String, TopicDescription> topics = await(adminClient.describeTopics(TOPICS).allTopicNames());
        Map<TopicPartition, OffsetSpec> request = topics.values().stream()
                .flatMap(topic -> topic.partitions().stream()
                        .map(partition -> new TopicPartition(topic.name(), partition.partition())))
                .collect(Collectors.toMap(Function.identity(), partition -> OffsetSpec.latest()));

        return await(adminClient.listOffsets(request).all()).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().offset()));
    }

    private List<ConsumerLagResponse> findConsumerLag(AdminClient adminClient, String groupId,
                                                      Map<TopicPartition, Long> endOffsets) {
        Map<TopicPartition, OffsetAndMetadata> committed =
                await(adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata());

        return committed.entrySet().stream()
                .filter(entry -> entry.getValue() != null && endOffsets.containsKey(entry.getKey()))
                .map(entry -> {
                    TopicPartition partition = entry.getKey();
                    long committedOffset = entry.getValue().offset();
                    long endOffset = endOffsets.get(partition);
                    return new ConsumerLagResponse(groupId, partition.topic(), partition.partition(),
                            committedOffset, endOffset, endOffset - committedOffset);
                })
                .sorted(Comparator.comparing(ConsumerLagResponse::topic)
                        .thenComparing(ConsumerLagResponse::partition))
                .toList();
    }

    private static <T> T await(KafkaFuture<T> future) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while querying Kafka", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Kafka is not available", e);
        }
    }
}
