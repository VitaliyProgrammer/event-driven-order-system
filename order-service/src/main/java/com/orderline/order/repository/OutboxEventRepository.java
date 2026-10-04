package com.orderline.order.repository;

import com.orderline.order.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("select e from OutboxEvent e where e.publishedAt is null order by e.createdAt limit 100")
    List<OutboxEvent> findPending();

    @Query("select e from OutboxEvent e order by e.createdAt desc limit 20")
    List<OutboxEvent> findRecent();

    @Query("""
            select e from OutboxEvent e
            where e.aggregateId = :orderId and e.publishedAt is not null
            order by e.createdAt desc limit 1
            """)
    Optional<OutboxEvent> findLastPublished(UUID orderId);
}
