package com.orderline.order.repository;

import com.orderline.order.entity.OrderStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderStatusChangeRepository extends JpaRepository<OrderStatusChange, UUID> {

    @Query("select c from OrderStatusChange c where c.orderId = :orderId order by c.changedAt")
    List<OrderStatusChange> findTimeline(UUID orderId);
}
