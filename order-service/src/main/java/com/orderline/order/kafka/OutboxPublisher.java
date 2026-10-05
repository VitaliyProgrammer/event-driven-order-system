package com.orderline.order.kafka;

import com.orderline.common.event.Topics;
import com.orderline.order.entity.OutboxEvent;
import com.orderline.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${orderline.outbox.publish-interval}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> events = outboxEventRepository.findPending();
        for (OutboxEvent event : events) {
            kafkaTemplate.send(Topics.ORDER_EVENTS, event.getAggregateId().toString(), event.getPayload()).join();
            event.markPublished();
            log.debug("Published {} {} for order {}", event.getEventType(), event.getId(), event.getAggregateId());
        }
    }
}
