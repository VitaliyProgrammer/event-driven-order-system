package com.orderline.order.service;

import com.orderline.common.event.Topics;
import com.orderline.order.dto.DeadLetterResponse;
import com.orderline.order.exception.DeadLetterNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterService {

    private static final List<String> DEAD_LETTER_TOPICS = List.of(Topics.ORDER_EVENTS_DLT, Topics.INVENTORY_EVENTS_DLT);
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(500);
    private static final Duration READ_DEADLINE = Duration.ofSeconds(5);

    private final ConsumerFactory<String, String> consumerFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public List<DeadLetterResponse> findAll() {
        List<DeadLetterResponse> deadLetters = new ArrayList<>();
        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            for (String topic : DEAD_LETTER_TOPICS) {
                readTopic(consumer, topic).forEach(record -> deadLetters.add(toResponse(record)));
            }
        }
        return deadLetters;
    }

    public DeadLetterResponse retry(String topic, int partition, long offset) {
        ConsumerRecord<String, String> record = findRecord(topic, partition, offset);
        String originalTopic = header(record, KafkaHeaders.DLT_ORIGINAL_TOPIC);
        if (originalTopic == null) {
            throw new IllegalStateException("Dead letter %s/%d/%d has no original topic header"
                    .formatted(topic, partition, offset));
        }
        kafkaTemplate.send(originalTopic, record.key(), record.value()).join();
        log.info("Republished dead letter {}/{}/{} to {}", topic, partition, offset, originalTopic);
        return toResponse(record);
    }

    private List<ConsumerRecord<String, String>> readTopic(Consumer<String, String> consumer, String topic) {
        List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                .map(info -> new TopicPartition(info.topic(), info.partition()))
                .toList();
        consumer.assign(partitions);
        consumer.seekToBeginning(partitions);
        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

        List<ConsumerRecord<String, String>> records = new ArrayList<>();
        Instant deadline = Instant.now().plus(READ_DEADLINE);
        while (!reachedEnd(consumer, endOffsets) && Instant.now().isBefore(deadline)) {
            consumer.poll(POLL_TIMEOUT).forEach(records::add);
        }
        return records;
    }

    private ConsumerRecord<String, String> findRecord(String topic, int partition, long offset) {
        if (!DEAD_LETTER_TOPICS.contains(topic)) {
            throw new DeadLetterNotFoundException(topic, partition, offset);
        }
        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            consumer.assign(List.of(topicPartition));
            consumer.seek(topicPartition, offset);

            Instant deadline = Instant.now().plus(READ_DEADLINE);
            while (Instant.now().isBefore(deadline)) {
                Optional<ConsumerRecord<String, String>> found = consumer.poll(POLL_TIMEOUT).records(topicPartition)
                        .stream()
                        .filter(record -> record.offset() == offset)
                        .findFirst();
                if (found.isPresent()) {
                    return found.get();
                }
            }
        }
        throw new DeadLetterNotFoundException(topic, partition, offset);
    }

    private static boolean reachedEnd(Consumer<String, String> consumer, Map<TopicPartition, Long> endOffsets) {
        return endOffsets.entrySet().stream()
                .allMatch(entry -> consumer.position(entry.getKey()) >= entry.getValue());
    }

    private static DeadLetterResponse toResponse(ConsumerRecord<String, String> record) {
        return new DeadLetterResponse(
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                header(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                header(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE),
                record.value(),
                Instant.ofEpochMilli(record.timestamp()));
    }

    private static @Nullable String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
