package com.orderline.order.exception;

import com.orderline.common.web.NotFoundException;

public class DeadLetterNotFoundException extends NotFoundException {

    public DeadLetterNotFoundException(String topic, int partition, long offset) {
        super("Dead letter %s/%d/%d not found".formatted(topic, partition, offset));
    }
}
