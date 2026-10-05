package com.orderline.common.kafka;

import com.orderline.common.event.Topics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;

    @Bean
    public KafkaAdmin.NewTopics orderlineTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(Topics.ORDER_EVENTS).partitions(PARTITIONS).replicas(1).build(),
                TopicBuilder.name(Topics.INVENTORY_EVENTS).partitions(PARTITIONS).replicas(1).build(),
                TopicBuilder.name(Topics.ORDER_EVENTS_DLT).partitions(1).replicas(1).build(),
                TopicBuilder.name(Topics.INVENTORY_EVENTS_DLT).partitions(1).replicas(1).build()
        );
    }
}
