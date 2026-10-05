package com.orderline.order.service;

import com.orderline.common.event.EventEnvelope;
import com.orderline.common.event.EventJsonMapper;
import com.orderline.order.entity.OutboxEvent;
import com.orderline.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final EventJsonMapper eventJsonMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void add(String eventType, UUID orderId, Object payload) {
        EventEnvelope event = eventJsonMapper.create(eventType, orderId, payload);
        outboxEventRepository.save(new OutboxEvent(event.eventId(), orderId, eventType, eventJsonMapper.write(event)));
    }
}
