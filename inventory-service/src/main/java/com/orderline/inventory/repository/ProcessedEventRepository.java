package com.orderline.inventory.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProcessedEventRepository {

    private final JdbcClient jdbcClient;

    public boolean markProcessed(UUID eventId) {
        int inserted = jdbcClient.sql("""
                        INSERT INTO processed_events (event_id, processed_at)
                        VALUES (:eventId, now())
                        ON CONFLICT (event_id) DO NOTHING
                        """)
                .param("eventId", eventId)
                .update();
        return inserted == 1;
    }
}
