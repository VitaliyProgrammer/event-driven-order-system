package com.orderline.common.kafka;

import com.orderline.common.event.InvalidEventException;
import com.orderline.common.event.Topics;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaErrorHandlingConfig {

    private static final long INITIAL_BACKOFF_MS = 1_000;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final int MAX_RETRIES = 3;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + Topics.DLT_SUFFIX, -1));

        ExponentialBackOff backOff = new ExponentialBackOff(INITIAL_BACKOFF_MS, BACKOFF_MULTIPLIER);
        backOff.setMaxAttempts(MAX_RETRIES);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(JacksonException.class, InvalidEventException.class);
        return errorHandler;
    }
}
